(ns micro-pm.core)

(defrecord User [id name email])
(defrecord Task [id summary desc category est points status assigned-to])
(defrecord Sprint [id start end points tasks])

(defonce task-id-count (atom 0))
(defn get-new-task-id [] (swap! task-id-count inc) @task-id-count)

(defonce app-state
  {:users (ref {})
   :tasks (ref {})
   :sprints (ref {})
   :backlog (ref [])})

(defn add-user
  [user]
  (let [users (:users app-state)]
    (when-not (contains? @users (:id user))
      (dosync
        (alter users assoc (:id user) user)))))

(defn add-task
  [{:keys [summary desc category est]}]
  (let [tasks (:tasks app-state)]
    (let [tid (get-new-task-id)
          task (->Task tid summary desc category est 0 "open" "no-one")]
      (dosync
        (alter tasks assoc tid task)))))

(defn assign-task
  [task user]
  (let [tasks (:tasks app-state)
        users (:users app-state)]
    (when (and
            (contains? @users (:id user))
            (contains? @tasks (:id task)))
      (dosync
        (alter tasks assoc (:id task)
          (assoc task :assigned-to (:id user)))))))

(defn get-user
  [user-id]
  (let [users (:users app-state)]
    (get @users user-id)))

(defn get-task
  [task-id]
  (let [tasks (:tasks app-state)]
    (get @tasks task-id)))

(defn tasks-by-user
  [user]
  (let [users (:users app-state)
        tasks (:tasks app-state)]
    (when (contains? @users (:id user))
      (filter #(= (:id user) (:assigned-to %)) (vals @tasks)))))

(defn update-task-status
  [task status]
  (let [tasks (:tasks app-state)]
    (when (contains? @tasks (:id task))
      (dosync
        (alter tasks assoc (:id task)
          (assoc task :status status))))))


(comment
  (add-user (map->User {:id "amitj" :name "amit"}))
  (add-user (map->User {:id "ddd" :name "noher amit"}))
  (add-task {:summary "work1" :desc "hard-work 1" :category "platform" :est 5})
  (add-task {:summary "work2" :desc "hard-work 2" :category "platform" :est 8})
  (add-task {:summary "work3" :desc "hard-work 3" :category "platform" :est 10})

  (assign-task
    (get (deref (:tasks app-state)) 3)
    (get (deref (:users app-state)) "ddd"))

  (get-user "ddd")
  (get-user "xddd")
  (keys (deref (:tasks app-state)))

  (get-task 3)
  (tasks-by-user (get-user "ddd")))

