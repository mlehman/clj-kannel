(ns clj-kannel.test.client
  (:use [clj-kannel.client] :reload)
  (:use [clojure.test]))

(deftest test-new-connection
  (testing "Create a connection with all defaults."
    (let [c (new-connection "foo" "bar")]
      (are [x k] (= x (k c))
	   "foo" :username
	   "bar" :password
	   "localhost" :hostname
	   13013 :port)))
  (testing "Create a connection with host."
    (let [c (new-connection "foo" "bar" :hostname "10.0.0.1")]
      (are [x k] (= x (k c))
	   "10.0.0.1" :hostname
	   13013 :port))))

(deftest test-new-sms-message
  (testing "Create a sms-message."
    (let [s (new-sms-message "5555551212" "Hello!")]
      (are [x k] (= x (k s))
	   "5555551212" :to
	   "Hello!" :text)))
  (testing "Create a sms-message with options."
    (let [s (new-sms-message "5555551212" "Hello!" :from "5555551234")]
      (are [x k] (= x (k s))
	   "5555551212" :to
	   "Hello!" :text
	   "5555551234" :from))))

(def test-connection (new-connection "foo" "bar"))

(defn stub-get
  [url params]
  (cond (= (:text "Succeed")) {:status 202 :body "OK"}
	:else {:status 500 :body "UNKNOWN"}))

(deftest test-send-sms
  (testing "Sending an SMS."
    (binding [clj-http.client/get stub-get]
      (is (= 202 (:status (send-sms test-connection
				    (new-sms-message "5555551212" "Succeed"))))))))