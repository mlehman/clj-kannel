(ns clj-kannel.client
  (:use [clj-kannel.util :only [merge-default-args]])
  (:require [clj-http.client :as http]))

;; Kannel Connection

(defrecord Connection [username password protocol hostname port])

(defn new-connection
  "Creates a new kannel gateway connection.
     username
       The username or account name. Must be username of the one 'sendsms-user' group in the Kannel
       configuration, or results in 'Authorization failed' reply.
     password
       Password associated with given username. Must match corresponding field in the 'sendsms-user'
       group of the Kannel configuration, or 'Authorization failed' is returned.

   The following are optional parameters:
     :protocol
       Either http or https.
     :hostname
       The ip address or domain name of the kannel gateway.
     :port
       The port for the kannel gateway."
  [username password & {:keys [protocol hostname port]
			:or {protocol "http"
			     hostname"localhost"
			     port 13013}
			:as extension-fields}]
  (Connection. username password protocol hostname port nil extension-fields))

;; Kannel SMS Message

(defrecord SmsMessage [to text])

(defn new-sms-message
  "Creates a new SMS message.
     to
       Phone number of the receiver. To send to multiple receivers, use a vector.
     text
       Contents of the message. The content can be more than 160 characters, but
       then sendsms-user group must have max-messages set more than 1.

   The following are optional parameters:
     :from
       Phone number of the sender. This field is usually overridden by the SMS Center,
       or it can be overridden by faked-sender variable in the sendsms-user group.
       If this variable is not set, smsbox global-sender is used."
  [to text & {:as extension-fields}]
  (SmsMessage. to text nil extension-fields))

;; Kannel Responses

(defrecord Response [status message])

;; Kannel Gateway
(defn- gateway-url
  [{:keys [protocol hostname port] :as connection}]
  (str protocol "://" hostname ":" port "/cgi-bin/sendsms"))

(defn- send-request
  [connection sms-message]
  (http/get (gateway-url connection)
	    {:query-params  (merge (select-keys connection [:username :password])
				   sms-message)
	     :throw-exceptions false}))

(defn- parse-response
  [{:keys [status body] :as http-response}]
  (Response. status body))

(defn send-sms
  "Sends an SMS message. Returns a Response."
  [connection sms-message]
  (->> sms-message
       (send-request connection)
       (parse-response)))