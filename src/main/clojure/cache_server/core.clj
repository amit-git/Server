(ns cache-server.core
  (:import [java.net ServerSocket]
           [java.io BufferedReader BufferedInputStream])
  (:require [clojure.java.io :as io :refer [reader writer]]
        [clojure.core.async :as async :refer [go]]))


; cache server functions
(def cache (atom {}))

(defn add-entry [k v] (swap! cache assoc k v) {k v})
(defn get-entry [k] (get @cache k))

(defn handle-cache-req
  [req]
  (let [tl (.trim req)
        fi (.indexOf tl " ") ; first space index to grab cmd (get/put)
        cmd (.substring tl 0 fi)
        cmd-args (.trim (.substring tl fi))
        entry (.split cmd-args "=")]
    (case cmd
      "get" (get-entry (first entry))
      "put" (add-entry (first entry) (second entry)))))


; client socket handling functions
(def ss (atom nil))
(def connections (atom []))

(defn app-start-state []
  {:connections (atom [])
   :ss (atom nil)
   :cache (atom {})})

(defn write-msg
  [cli-sock-writer msg]
  (doto cli-sock-writer
    (.write msg)
    (.flush)))

(defn write-prompt [cli-sock-writer] (write-msg cli-sock-writer "\nPrompt ::> "))
(defn welcome-msg [cli-sock-writer] (write-msg cli-sock-writer "Welcome to clojure service") (write-prompt cli-sock-writer))

(defn send-response
  [cli-sock-writer msg]
  (write-msg cli-sock-writer msg)
  (write-prompt cli-sock-writer))

(defn handle-cli
  [cli-sock]
  (let [ci (io/reader cli-sock)
        co (io/writer cli-sock)]
    (welcome-msg co)
    ; client request handling loop
    (try
      (while true
        (let [br (BufferedReader. ci)
              line (.readLine br)]
          (do
            (println "Req - " line)
            (send-response co (str (handle-cache-req line))))))
      (catch Exception e (pr-str "Client socket exception - " (.getMessage e))))))

(defn start-server
  [port]
  (future
    (let [sss (ServerSocket. port)]
      (reset! ss sss)
      (loop []
        (let [cs (.accept sss)]
          (swap! connections conj cs)
          (async/go (handle-cli cs)))
        (recur)))))

(defn stop-server []
  (do
    (doseq [conn @connections] (.close conn))
    (if (not (nil? @ss)) (.close @ss))
    (reset! ss nil)
    (reset! connections [])))

(comment
  (start-server 9000)
  (stop-server)
  (async/go (str "Some new funda here"))
  (pr-str (handle-cache-req "put color=blue,red"))
  (handle-cache-req "get name")
  (reset! cache {})

  )