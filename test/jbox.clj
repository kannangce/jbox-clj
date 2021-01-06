(ns test.jbox
    (:require [jbox.client :as j])
    (:require [clojure.test :refer [deftest is testing use-fixtures]]))



(deftest ^:integration
           all-ops
           (def cfg (j/init
                      {:box-id "box_7ea987436ae76408e8b4"
                       :token  "f37a491d-e07b-4241-b5a5-1b7cca22d628"}))

           (j/with-config cfg
                          (let [created (do
                                          (j/create "u1" {:username "uname1" :email "mail1@a.com"})
                                          (j/create "u2" {:username "uname2" :email "mail2@a.com"}))
                                created-id2 (:_id created)
                                fetched-1 (j/fetch "u1")
                                fetch-all (j/fetch)
                                search-fetch (j/fetch {:q "username:uname1"})
                                fetch-all-after-delete-1 (do (j/delete {:q "username:uname1"})
                                                             (j/fetch))
                                fetch-after-edit-1 (do (j/edit created-id2 {:username "uname3" :email "mail2@a.com"})
                                                       (j/fetch {:q "username:uname3"}))
                                fetch-after-delete-all (do (j/delete {:q "username:uname3"})
                                                           (j/fetch))]

                               (is (= 1 (count fetched-1) "There is only one entry expected here."))
                               (is (= 2 (count fetch-all)))
                               (is (= 1 (count search-fetch)))
                               (is (= 1 (count fetch-all-after-delete-1)))
                               (is (= 1 (count fetch-all-after-delete-1)))
                               (is (= 0 (count fetch-after-delete-all))))
                          ))