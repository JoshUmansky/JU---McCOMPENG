#include <pthread.h>
#include <semaphore.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

//To compile: gcc -o A2 A2.c -lpthread

#define NUM_CHAIRS 3
#define NUM_STUDENTS 5

// Global variables
sem_t ta_sleep;
sem_t student_sem;
pthread_mutex_t chair_mutex;
pthread_mutex_t helping_time_mutex;
int waiting_students = 0;
int helping_time = 0; //Used to sync the help time of the student and the TA, cant randomly generate both

void* student(void* num) {
    int id = *(int*)num;

    while (1) {
        //Simulate a student programming
        printf("Student %d is programming.\n", id);
        sleep(rand() % 5 + 1); //Random programming time

        //Attempt to get help from the TA
        pthread_mutex_lock(&chair_mutex); // Lock the chair mutex
        if (waiting_students < NUM_CHAIRS) { // Check if there are available chairs
            waiting_students++; //Sit
            printf("Student %d is waiting. Waiting students: %d\n", id, waiting_students);
            pthread_mutex_unlock(&chair_mutex); //Unlock, am now waiting
            
            sem_post(&student_sem); // Notify the TA that a student is waiting
            sem_wait(&ta_sleep); // Wait for the TA to wake up

            pthread_mutex_lock(&helping_time_mutex);
            helping_time = rand() % 5 + 1; //Random help time
            pthread_mutex_unlock(&helping_time_mutex);

            //When TA eventually wakes up (Ta can also just be helping someone else), get help
            printf("Student %d is getting help from the TA.\n", id);
            sleep(helping_time); //Random helping time
            printf("Student %d is done getting help.\n", id);
        } else {
            // No chairs available, continue programming
            printf("Student %d found no available chairs. Continuing to program.\n", id);
            pthread_mutex_unlock(&chair_mutex);
        }
    }
    return NULL;
}

void* ta(void* arg) {
    while (1) {
        // Wait for a student to arrive
        sem_wait(&student_sem); //Freed by the sem_post in student

        // Help the student
        pthread_mutex_lock(&chair_mutex); //Take a student, Stop another student from sitting for a second
        waiting_students--; 
        pthread_mutex_unlock(&chair_mutex); //New chair open, allow new student to sit

        // Notify the student
        sem_post(&ta_sleep);

        // Simulate helping the student

        pthread_mutex_lock(&helping_time_mutex);
        int current_help_time = helping_time;
        pthread_mutex_unlock(&helping_time_mutex);

        printf("TA is helping a student.\n");
        sleep(current_help_time); //Random helping time synced with the student
        printf("TA is done helping a student.\n");
    }

    return NULL;
}

int main() {
    pthread_t ta_thread;
    pthread_t student_threads[NUM_STUDENTS];
    int student_ids[NUM_STUDENTS];

    // Initialize semaphores and mutex
    sem_init(&ta_sleep, 0, 0);
    sem_init(&student_sem, 0, 0);
    pthread_mutex_init(&chair_mutex, NULL);
    pthread_mutex_init(&helping_time_mutex, NULL);

    // Create TA thread
    pthread_create(&ta_thread, NULL, ta, NULL);

    // Create student threads
    for (int i = 0; i < NUM_STUDENTS; i++) {
        student_ids[i] = i + 1;
        pthread_create(&student_threads[i], NULL, student, (void*)&student_ids[i]);
    }

    // Join student threads
    for (int i = 0; i < NUM_STUDENTS; i++) {
        pthread_join(student_threads[i], NULL);
    }

    // Join TA thread
    pthread_join(ta_thread, NULL);

    // Destroy semaphores and mutex
    sem_destroy(&ta_sleep);
    sem_destroy(&student_sem);
    pthread_mutex_destroy(&chair_mutex);
    pthread_mutex_destroy(&helping_time_mutex);

    return 0;
}

