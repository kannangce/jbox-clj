(ns jbox.client
  (:require [clj-http.client :as http-client])
  (:require [clojure.data.json :as json]))


(def ^:private default-base-url "https://jsonbox.io/")
(def ^:private *config* {:base-url default-base-url})
(def ^:private default-options
  {:headers              {:accept :json}
   :content-type         :json
   :response-interceptor (fn [resp ctx] (println resp ctx))})


(defn init
  "Initializes the client"
  [cfg]
  (alter-var-root #'*config* merge cfg))

(defn invoke
  ([http-fn end-point options]
   (do
     (let [url (str (:base-url *config*) end-point)
           final-options (merge options default-options)]
       (do
         (println url)
         (http-fn url final-options)))))
  ([http-fn end-point]
   (-> (invoke http-fn end-point nil)
       :body
       json/read-json)))

(defn create
  ([obj-type obj-to-create]
   (invoke http-client/post
           (str (:box-id *config*) "/" obj-type)
           {:body (json/json-str obj-to-create)}))
  ([obj-to-create]
   (create nil obj-to-create)))

(defn fetch
  [obj-id]
  (invoke http-client/get
          (str (:box-id *config*) "/" obj-id)))

(defn list
  ([]
   (invoke http-client/get (:box-id *config*)))
  ([obj-type]
   (invoke http-client/get
           (str (:box-id *config*) "/" obj-type)))
  ([obj-type params]
   (invoke http-client/get
           (str (:box-id *config*) "/" obj-type)) {:query-params params}))

(defn update
  [obj-id obj-to-update]
  (invoke http-client/put (str (:box-id *config*) "/" obj-id) {:body obj-to-update}))

(defn delete
  ([^String obj-id]
   (invoke http-client/delete (str (:box-id *config*) "/" obj-id))))

(defn delete-multiple
  ([criteria]
   (invoke http-client/delete (str (:box-id *config*) {:query-params criteria}))))

(defn box-meta
  []
  (invoke http-client/get (str "_meta" "/" (:box-id *config*))))

(init {:box-id "box_4eb742d8eb29e6c8d1c6"})

*ns*
;
;(get-config {:box-id "box_4eb742d8eb29e6c8d1c6"})
;
;

(vector {:username "uname" :email "mail@a.com"})
(create "users" {:username "uname" :email "mail@a.com"})
(create  {:username "uname" :email "mail@a.com"})
(create "users" [{:username "uname" :email "mail@a.com"} {:username "uname1" :email "mail@a.com"}])
(list)
(box-meta)


(http-client/post
  "https://jsonbox.io/box_4eb742d8eb29e6c8d1c6/users"
  {; :debug true
   ;:debug-body true
   :response-interceptor (fn [resp ctx] (println resp ctx))
   :debug                true
   :body                 (json/json-str [{:username "uname" :email "mail@a.com"}])
   :content-type         :json
   })

;
;(:base-url *config*)
;
*config*
;
;(alter-var-root #'*config* {:one 1})
;
*ns*