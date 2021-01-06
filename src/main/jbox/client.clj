(ns jbox.client
  (:require [clj-http.client :as http-client])
  (:require [clojure.data.json :as json]))


(def ^:private default-base-url "https://jsonbox.io/")
(def ^:private ^:dynamic *config* {:base-url default-base-url})
(def ^:private default-options
  {:headers      {:accept :json}
   ;:debug        true
   ;:response-interceptor (fn [resp ctx] (println resp ctx))
   :content-type :json})

(defn classify-ip
  ([ip &_]
   (classify-ip ip))
  ([ip]
   (cond (map? ip) :map (string? ip) :str)))

(defn init
  "Initializes the client"
  [cfg]
  (merge *config* cfg))

(defn invoke
  ([http-fn end-point options]
   (do
     (when
       (nil? (:box-id *config*))
       (throw (RuntimeException. "Client not initialized. Invoke using 'with-config'")))
     (let [url (str (:base-url *config*) end-point)
           value (:token *config*)
           auth-headers (if (nil? value) {} {:X-API-KEY value})
           final-options (merge options
                                (update-in default-options
                                           [:headers] merge auth-headers))]
       (do
         (-> (http-fn url final-options)
             :body
             json/read-json)))))
  ([http-fn end-point]
   (invoke http-fn end-point nil)))

;; Create
(defn create
  ([collection obj-to-create]
   "Create object under specific collection"
   (invoke http-client/post
           (str (:box-id *config*) "/" collection)
           {:body (json/json-str obj-to-create)}))
  ([obj-to-create]
   "Creates the given object"
   (create nil obj-to-create)))


;; Fetch
(defmulti fetch-with-criteria classify-ip)

(defmethod fetch-with-criteria :str [obj-id]
  (invoke http-client/get (str (:box-id *config*) "/" obj-id)))

(defmethod fetch-with-criteria :map [criteria]
  (invoke http-client/get (str (:box-id *config*)) {:query-params criteria}))


(defn fetch
  ([]
   "Fetch all"
   (invoke http-client/get (:box-id *config*)))
  ([fetch-criteria]
   "Fetch specific object(s). fetch-criteria can be and id or search criteria"
   (fetch-with-criteria fetch-criteria)))

;; Update
(defn edit
  [obj-id obj-to-update]
  "Update the object with given id"
  (invoke http-client/put (str (:box-id *config*) "/" obj-id) {:body (json/json-str obj-to-update)}))


;; Delete
(defmulti delete classify-ip)

(defmethod delete :str [obj-id]
  "Delete object with given id"
  (invoke http-client/delete (str (:box-id *config*) "/" obj-id)))

(defmethod delete :map [criteria]
  "Delete all objects matching the given criteria"
  (invoke http-client/delete (str (:box-id *config*)) {:query-params criteria}))

(defn box-meta
  "Gets meta data of the box"
  []
  (invoke http-client/get (str "_meta" "/" (:box-id *config*))))

(defmacro with-config [config & executables]
  `(binding [*config* ~config]
     ~@executables))