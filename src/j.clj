(ns j
  (:require [clojure.datafy]
            [clojure.tools.namespace.repl :as ctnr]
            [lambdaisland.classpath.watch-deps :as watch-deps]
            [portal.api :as portal]
            [lambdaisland.classpath :as cp]
            [medley.core]
            [nextjournal.clerk :as clerk]))

(defn add-lib [deps-coord]
          (let [o (first deps-coord)])
          @(cp/update-classpath!
            `{:aliases [:dev]
              :extra {:deps ~deps-coord}}))
(def set-ns-refresh-dirs ctnr/set-refresh-dirs)
(def refresh ctnr/refresh)
(def refresh-all ctnr/refresh-all)
(def datafy clojure.datafy/datafy)
(def nav clojure.datafy/nav)
(def clear-portal portal/clear)
(def close-portal portal/close)
(def open-portal portal/open)
(def start-portal portal/start)
(def submit-to-portal portal/submit)
(def submit-datafied (comp portal/submit clojure.datafy/datafy))
(defn target-portal [] (add-tap #'portal/submit))
(defn untarget-portal [] (remove-tap #'portal/submit))
(defn <-portal [p-var] (prn (deref p-var)))
(defn cold-dev!
  []
  (watch-deps/start! {:aliases [:dev]})
  (clerk/serve! {:browse true})
  (portal/tap)
  (def p (portal/open))
  (add-tap #'submit-datafied))

(defmacro qrq [pns] (let [n (symbol (str "tools.drilling." (name pns)))]
                       `(require (quote ~n))))
(defn jump
  [quoted-namespace]
  (do (require quoted-namespace)
      (in-ns quoted-namespace)))

(defn uptime
  "Return the date this REPL (Java process) was started."
  []
  (java.util.Date. (- (.getTime (java.util.Date.))
                      (.getUptime (java.lang.management.ManagementFactory/getRuntimeMXBean)))))

(def ls-fns (map (comp keyword first) (ns-publics 'j)))

(defmacro jit
  "Just in time loading of dependencies, so you can run anything on the classpath in the user namespace. Just wrap it in 2 parens, (( dev/up))"
  [sym]
  `(requiring-resolve '~sym))
