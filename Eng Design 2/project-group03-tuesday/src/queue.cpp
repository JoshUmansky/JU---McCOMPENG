#include <iostream>
#include <vector>
#include <mutex>
#include <condition_variable>
#include <thread>
#include <queue>

class Queue {
    private:
        std::queue<std::vector<float>> buffer;
        const size_t maxSize;
        std::mutex mtx; // Mutex for synchronizing access to the queue
        std::condition_variable Full; // Condition variable to wait on when the queue is full
        std::condition_variable Empty; // Condition variable to wait on when the queue is empty
        bool producing = true;

    public:
        Queue(size_t maxSize) : maxSize(maxSize) {}

        void produce(std::vector<float> item) {
            std::unique_lock<std::mutex> lock(mtx); // Lock the mutex before modifying the queue
            Full.wait(lock, [this] { return buffer.size() < maxSize; }); // Wait until there is space in the queue
            buffer.push(item); // Insert the item at the tail
            Empty.notify_one(); // Notify a consumer that an item is available
        }

        std::vector<float> consume() {
            std::unique_lock<std::mutex> lock(mtx); // Lock the mutex before modifying the queue
            Empty.wait(lock, [this] { return !buffer.empty(); }); // Wait until there is an item to consume
            std::vector<float> item = std::move(buffer.front()); 
            buffer.pop(); // Remove the item from the head
            Full.notify_one(); // Notify a producer that there is space in the queue
            return item;
        }

        void stopProducing() {
            std::unique_lock<std::mutex> lock(mtx); // Lock the mutex before modifying the queue
            Empty.wait(lock, [this] { return buffer.empty(); }); // Wait until the buffer is empty
            producing = false; // Set producing to false
            // Notify all waiting threads to re-check their conditions
            Full.notify_all();
            Empty.notify_all();
        }

        bool isProducing() {
            std::lock_guard<std::mutex> lock(mtx);
            return producing || buffer.empty();
        }

};