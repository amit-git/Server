(ns cache-server.core
  (:import [java.net ServerSocket]
           [java.io BufferedReader BufferedInputStream])
  (:require [clojure.java.io :as io :refer [reader writer]]
            [clojure.core.async :as async :refer [go]]))

; cache server functions
(defn add-entry [cache k v] (swap! cache assoc k v) {k v})
(defn get-entry [cache k] (get @cache k))

(defn ^:private handle-cache-req
  [cache req]
  (let [tl (.trim req)
        fi (.indexOf tl " ") ; first space index to grab cmd (get/put)
        cmd (.substring tl 0 fi)
        cmd-args (.trim (.substring tl fi))
        entry (.split cmd-args "=")]
    (case cmd
      "get" (get-entry cache (first entry))
      "put" (add-entry cache (first entry) (second entry)))))

(defn handle-req
  [cache req]
  (if (and
        (or
          (.startsWith req "get")
          (.startsWith req "put"))
        (.contains req " "))
    (handle-cache-req cache req)
    ""))

; client socket handling functions
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

(defn handle-cli-sock
  [cli-sock cache]
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
            (send-response co (str (handle-req cache line))))))
      (catch Exception e (pr-str "Client socket exception - " (.getMessage e))))))

(defn start-server
  [port]
  (let [app-state (app-start-state)]
    (future
      (let [sss (ServerSocket. port)]
        (reset! (:ss app-state) sss)
        (loop []
          (let [cs (.accept sss)]
            (swap! (:connections app-state) conj cs)
            (async/go (handle-cli-sock cs (:cache app-state))))
          (recur))))
    app-state))

(defn stop-server
  [app-state]
  (let [{:keys [connections ss cache]} app-state]
    (do
      (doseq [conn @connections] (.close conn))
      (if (not (nil? @ss)) (.close @ss))
      (reset! ss nil)
      (reset! cache {})
      (reset! connections []))))

(comment
  ; simple in-memory caching server
  ; sample input "put color=blue, red, black"
  ; sample input "get color"
  (def app (start-server 9000))
  (stop-server app)

  )