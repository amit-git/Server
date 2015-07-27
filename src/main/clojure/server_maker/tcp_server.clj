(ns server-maker.tcp-server
  (:require [clojure.core.async :as a :refer [>! <! >!! <!! go chan buffer close! thread alts! alts!! timeout]])
  (:import [java.io BufferedReader InputStreamReader PrintWriter]
           [java.net ServerSocket]))

(def running-flag (atom true))
(def ssocket (atom nil))

(defn handle-client
  [socket req-handler]
  (with-open [in (BufferedReader. (InputStreamReader. (.getInputStream socket)))
              out (PrintWriter. (.getOutputStream socket))]
    (while true
      (let [line (.readLine in)]
        (if (not @running-flag) (throw (Exception. "Server disconnected")))
        (.write out (req-handler line))
        (.flush out)))))

(defn build-server
  [port request-handler]
  (pr (str "Listening on port " port))
  (reset! ssocket (java.net.ServerSocket. port))
  (reset! running-flag true)
  (future (with-open [ss @ssocket]
            (while @running-flag
              (let [cs (.accept ss)]
                (if (not @running-flag) (throw (Exception. "Server Shutting Down")))
                (future (handle-client cs request-handler)))))))

(defn stop-server []
  (if (not (nil? @ssocket)) (.close @ssocket))
  (reset! ssocket nil)
  (reset! running-flag false))

