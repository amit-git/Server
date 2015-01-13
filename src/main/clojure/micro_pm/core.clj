(ns micro-pm.core)

(defrecord User [id name email])
(defrecord Task [id summary desc category est points status assigned-to sprint-id])
(defrecord Sprint [id start end points])

(defonce task-id-count (atom 0))
(defonce sprint-id-count (atom 0))
(defn get-new-task-id [] (swap! task-id-count inc) @task-id-count)
(defn get-new-sprint-id [] (swap! sprint-id-count inc) @sprint-id-count)

(defonce app-state
  {:users (ref {})
   :tasks (ref {})
   :sprints (ref {})})

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
          task (->Task tid summary desc category est 0 "open" "no-one" "backlog")]
      (dosync
        (alter tasks assoc tid task)))))

(defn assign-task
  [task user]
  (let [tasks (:tasks app-state)
        users (:users app-state)]
    (when (and (contains? @users (:id user)) (contains? @tasks (:id task)))
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

(defn create-sprint
  [start end points]
  (let [sprint-id (get-new-sprint-id)
        new-sprint (->Sprint sprint-id start end points)
        sprints (:sprints app-state)]
    (dosync
      (alter sprints assoc sprint-id new-sprint))
    new-sprint))

(defn sprint-add-task
  [sprint task]
  {:pre [(instance? Sprint sprint) (instance? Task task)]}
  (let [sprints (:sprints app-state)
        tasks (:tasks app-state)]
    (when (and (contains? @sprints (:id sprint)) (contains? @tasks (:id task)))
      (dosync
        (alter tasks assoc (:id task)
          (assoc task :sprint-id (:id sprint)))))))

(defn sprint-remove-task
  [sprint task]
  {:pre [(instance? Sprint sprint) (instance? Task task)]}
  (let [sprints (:sprints app-state)
        tasks (:tasks app-state)]
    (when (contains? @sprints (:id sprint))
      (dosync
        (alter tasks assoc (:id task)
          (assoc task :sprint-id "backlog"))))))

(defn get-sprint
  [sprint-id]
  (let [sprints (:sprints app-state)]
    (get @sprints sprint-id)))

(defn tasks-in-sprint
  [sprint]
  {:pre [(instance? Sprint sprint)]}
  (let [sprints (:sprints app-state)
        tasks (:tasks app-state)]
    (when (contains? @sprints (:id sprint))
      (filter #(= (:id sprint) (:sprint-id %)) (vals @tasks)))))

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
  (keys (deref (:sprints app-state)))

  (get-task 1)
  (tasks-by-user (get-user "ddd"))
  (create-sprint "now" "d+10" 15)
  (sprint-add-task (get-sprint 2) (get-task 3))
  (sprint-add-task (get-sprint 2) (get-task 1))
  (sprint-add-task (get-sprint 2) (get-task 2))
  (sprint-remove-task (get-sprint 2) (get-task 1))
  (count (tasks-in-sprint (get-sprint 2)))

  )

