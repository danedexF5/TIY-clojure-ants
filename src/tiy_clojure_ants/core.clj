(ns ants-clojure.core
  (:require [clojure.java.io :as io])
  (:import [javafx.application Application]
           [javafx.fxml FXMLLoader]
           [javafx.scene Scene]
           [javafx.scene.paint Color]
           [javafx.animation AnimationTimer])
  (:gen-class :extends javafx.application.Application))

;Store the color of each ant inside itself with the :color key. They should start off as black. Then make draw-ants use
;this value to draw the color of the ant instead of hard-coding it as Color/BLACK.

(def width 800)
(def height 600)
(def ant-count 100)
(def ants (atom nil))
(def last-timestamp (atom 0))

(defn create-ants []
  (for [i (range ant-count)]
    {:x (rand-int width)
     :y (rand-int height)
     :color (Color/BLACK)}))

(defn x [ant] (:x ant))
(defn y [ant] (:y ant))

(defn close [ant]
  (filter #(and
            (<= (Math/abs (- (x %) (x ant))) 10)
            (<= (Math/abs (- (y %) (y ant))) 10))
          @ants))

(defn draw-ants! [context]
  (.clearRect context 0 0 width height)
  (doseq [ant @ants]
    (.setFill context (:color ant))
    (.fillOval context (:x ant) (:y ant) 5 5)))



;Write a function called aggravate-ant. It should filter over all the ants to find the ones that are within 10 pixels of
;it (you can determine this by calculating the difference between the ants' :x and :y in the filter function,
;using the Math/abs function to get the absolute value). If the result of the filter is one,
;then set the ant's :color to Color/BLACK (because that one result is the ant itself!). Otherwise, set it to Color/RED.))


(defn aggravate-ant [this-ant]
  (if (= 1 (count (close this-ant)))
    (assoc this-ant :color Color/BLACK)
    (assoc this-ant :color Color/RED)))


(defn random-step []
  (- (* 6 (rand)) 3))

(defn move-ant [ant]
  ;(Thread/sleep 1)
  (assoc ant
    :x (+ (random-step) (:x ant))
    :y (+ (random-step) (:y ant))))

(defn fps [now]
  (let [diff (- now @last-timestamp)
        diff-seconds (/ diff 1000000000)]
    (int (/ 1 diff-seconds))))

(defn -start [app stage]
  (let [root (FXMLLoader/load (io/resource "main.fxml"))
        scene (Scene. root width height)
        canvas (.lookup scene "#canvas")
        context (.getGraphicsContext2D canvas)
        fps-label (.lookup scene "#fps")
        timer (proxy [AnimationTimer] []
                (handle [now]
                  (.setText fps-label (str (fps now)))
                  (reset! last-timestamp now)
                  (reset! ants (doall (pmap move-ant @ants)))
                  (reset! ants (doall (pmap aggravate-ant @ants)))
                  (draw-ants! context)))]
    (reset! ants (create-ants))
    (.setTitle stage "Ants")
    (.setScene stage scene)
    (.show stage)
    (.start timer)))

(defn -main []
  (Application/launch ants_clojure.core (into-array String [])))
