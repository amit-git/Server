(ns micro-pm.core)

(defrecord User [id name email])
(defrecord Task [id summary desc category est points status assigned-to])
(defrecord Sprint [id start end points tasks])

(defonce task-id-count (atom 0))
(defn get-new-task-id [] (swap! task-id-count inc) @task-id-count)

(defn build-app-state []
  {:users (atom {})
   :tasks (atom {})
   :sprint (atom {})})

(defn add-user [app-state user]
  (if-let [users (:users app-state)]
    (when (:id user)
      (swap! users assoc (:id user) user))))

(defn add-task [app-state {:keys [summary desc category]}]
  (if-let [tasks (:tasks app-state)]
    (let [tid (get-new-task-id)
          task (->Task tid summary desc :category nil nil nil nil)]
      (swap! tasks assoc tid task))))

(defn assign-task
  [app-state task user]
  (let [tasks (:tasks app-state)
           users (:users app-state)
           sprint (:sprint app-state)]
    (if (and
          (not (nil? tasks))
          (not (nil? users))
          (not (nil? sprint))
          (contains? @tasks task)
          (contains? @users user))
      (swap! sprint assoc task user))))

(defn tasks-by-user
  [app-state user]
  (let [sprint (:sprint app-state)]
    (when (not (nil? sprint))
      (filter (fn [entry] (= (val entry) user)) @sprint))))

(defn update-task-status
  [app-state task status]
  )

(comment
  (def app-state (build-app-state))
  (add-user app-state (map->User {:id "amitj" :name "amit"}))
  (add-task app-state {:summary "work1" :desc "hard-work 1" :category "platform"})

  (add-task app-state {:summary "work2" :desc "hard-work 2"})
  (add-task app-state {:summary "work3" :desc "hard-work 3" :category "platform"})

  (deref (:users app-state))
  (deref (:tasks app-state))

  (assign-task app-state
    (map->Task {:summary "work3" :desc "hard-work 3"})
    (map->User {:id "amitc"}))

  ; update work2 = done

  (swap! (:tasks app-state)
    (fn [tasks]
      (map
        (fn [task]
          (if (= (:summary task) "work2")
            (assoc task :status "DONE")
            task ))
       tasks)))

  (deref (:sprint app-state))

  (get-new-task-id)


  )

