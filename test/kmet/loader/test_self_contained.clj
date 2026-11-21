(ns kmet.loader.test-self-contained
  "Guard: the loader library stays self-contained. Its namespaces may
   require other kmet.loader.* namespaces and third-party deps, but nothing
   else under kmet.* — that is what lets this project stand alone. The
   reverse direction (a host layer reaching into kmet.loader.*) belongs to
   the host repository's guard."
  (:require [clojure.string :as str]
            [clojure.test :as t :refer [deftest is]]
            [babashka.fs :as fs]))

(def ^:private reader-views
  "Both reader views: a #? branch can hide a require from the other view."
  [#{:bb :clj} #{:jolt :clj}])

(defn- loader-files []
  (->> (fs/list-dir "src/kmet/loader")
       (filter #(re-find #"\.(?:clj[ca]?|jolt)$" (str %)))
       (map str)))

(defn- ns-forms
  "The ns form of PATH under each reader view, or nil when the first form is
   not an ns form. Throws when the file cannot be read — a guard that cannot
   read its file must never silently check nothing."
  [path]
  (for [features reader-views]
    (try
      (let [form (read-string {:read-cond :allow :features features} (slurp path))]
        (when (and (seq? form) (= 'ns (first form))) form))
      (catch Exception e
        (throw (ex-info (str "self-containment guard cannot read " path)
                        {:type ::unreadable :path (str path)} e))))))

(defn- foreign-kmet-requires [path]
  (for [form (ns-forms path)
        :when form
        clause (rest form)
        :when (and (seq? clause) (= :require (first clause)))
        spec (rest clause)
        :let [ns-sym (cond
                       (symbol? spec) spec
                       (vector? spec) (first spec)
                       (seq? spec) (first spec))]
        :when (and ns-sym
                   (str/starts-with? (str ns-sym) "kmet.")
                   (not (str/starts-with? (str ns-sym) "kmet.loader.")))]
    (str ns-sym)))

(deftest loader-is-self-contained
  (doseq [f (loader-files)
          :let [deps (distinct (foreign-kmet-requires f))]]
    (is (empty? deps)
        (str f " must not require kmet.* namespaces besides kmet.loader.*, found: "
             (vec deps)))))
