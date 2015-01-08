(ns micro-pm.core)

(defrecord User [id name email])
(defrecord Task [id summary desc category est points status assigned-to])
(defrecord Sprint [id start end points tasks])

(defonce task-id-count (atom 0))
(defn get-new-task-id [] (swap! task-id-count inc) @task-id-count)

(defn build-app-state []
  {:users (ref {})
   :tasks (ref {})
   :sprints (ref {})
   :backlog (ref [])})

(defn add-user
  [app-state user]
  (when-let [users (:users app-state)]
    (when-not (contains? @users (:id user))
      (dosync
        (alter users assoc (:id user) user)))))

(defn add-task [app-state {:keys [summary desc category est]}]
  (when-let [tasks (:tasks app-state)]
    (let [tid (get-new-task-id)
          task (->Task tid summary desc category est 0 "open" "no-one")]
      (dosync
        (alter tasks assoc tid task)))))

(defn assign-task
  [app-state task user]
  (when-let [tasks (:tasks app-state)]
    (when-let [users (:users app-state)]
      (when (and
              (contains? @users (:id user))
              (contains? @tasks (:id task)))
        (dosync
          (alter tasks assoc (:id task)
            (assoc task :assigned-to (:id user))))))))

(defn get-user
  [app-state user-id]
  (when-let [users (:users app-state)]
    (get @users user-id)))

(defn get-task
  [app-state task-id]
  (when-let [tasks (:tasks app-state)]
    (get @tasks task-id)))

(defn tasks-by-user
  [app-state user]
  (when-let [users (:users app-state)]
    (when-let [tasks (:tasks app-state)]
      (when (contains? @users (:id user))
        (filter #(= (:id user) (:assigned-to %)) (vals @tasks))))))

(defn update-task-status
  [app-state task status]
  (when-let [tasks (:tasks app-state)]
    (when (contains? @tasks (:id task))
      (dosync
        (alter tasks assoc (:id task)
          (assoc task :status status))))))

(comment
  (def app-state (build-app-state))
  (add-user app-state (map->User {:id "amitj" :name "amit"}))
  (add-user app-state (map->User {:id "ddd" :name "noher amit"}))
  (add-task app-state {:summary "work1" :desc "hard-work 1" :category "platform" :est 5})
  (add-task app-state {:summary "work2" :desc "hard-work 2" :category "platform" :est 8})
  (add-task app-state {:summary "work3" :desc "hard-work 3" :category "platform" :est 10})

  (add-task {} {:summary "work1" :desc "hard-work 1" :category "platform" :est 5})

  (keys (deref (:tasks app-state)))
  (keys (deref (:users app-state)))

  (assign-task app-state
    (get (deref (:tasks app-state)) 13)
    (get (deref (:users app-state)) "ddd"))

  (get-user app-state "ddd")
  (get-user app-state "xddd")

  (update-task-status app-state (get-task app-state 13) "pending")

  (get-task app-state 13)

  (tasks-by-user app-state (get-user app-state "ddd"))
  (tasks-by-user app-state "ddd")

  ; update work2 = done

  (swap! (:tasks app-state)
    (fn [tasks]
      (map
        (fn [task]
          (if (= (:summary task) "work2")
            (assoc task :status "DONE")
            task))
        tasks)))

  (deref (:sprint app-state))

  (def t1 {:id1 "value1" :id2 "value2" :id3 "value3"})

  (->> t1
    (map (fn [[k v]] {k (.toUpperCase v)}))
    (into {})
    )
  (empty? nil)

  )

