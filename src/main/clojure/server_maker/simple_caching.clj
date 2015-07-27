(ns server-maker.simple-caching
  (:require [clojure.core.async :as a :refer [>! <! >!! <!! go chan buffer close! thread alts! alts!! timeout]])
  (:use server-maker.tcp-server))

(defn uppercase-line-handler
  [req-line]
  (if (= req-line "bye") (throw (Exception. "signing off")))
  (format "%s\n" (.toUpperCase req-line)))

(defn lowercase-line-handler
  [req-line]
  (if (= req-line "stop") (throw (Exception. "signing off")))
  (format "%s\n" (.toLowerCase req-line)))

(defn make-cache-handler []
  (let [data (atom {})]
    (fn [input-line]
      (let [line (.trim input-line)]
        (if-let [set-matches (re-matches #"^set (\w+)=(.*)" line)]
          (do
            (reset! data (assoc @data (nth set-matches 1) (nth set-matches 2)))
            (str "Saved\n"))
          (if-let [get-matches (re-matches #"^get (\w+)" line)]
            (str (get @data (nth get-matches 1)) "\n")))))))

(comment
  (build-server 8888 (make-cache-handler))
  (stop-server)
  (build-server 8888 uppercase-line-handler))
