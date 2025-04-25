#include <pthread.h>
#include <stdlib.h>
#include <stdio.h>

#define NUM_THREADS (6U)
#define NUM_DEPOSIT (3U)

int amount = 0;
pthread_mutex_t mut;

void *deposit(void *raw)
{
    int deposit_amount = *(int *)raw;

    pthread_mutex_lock(&mut);
    amount += deposit_amount;
    pthread_mutex_unlock(&mut);

    printf("Deposit amount = %d\n", amount);
    pthread_exit(0);
}

void *withdraw(void *raw)
{
    int withdrawal_amount = *(int *)raw;

    pthread_mutex_lock(&mut);
    amount -= withdrawal_amount;
    pthread_mutex_unlock(&mut);

    printf("Withdrawal amount = %d\n", amount);
    pthread_exit(0);
}

int main(int argc, char **argv)
{
    if (argc != 3)
    {
        printf("error (invalid number of arguments - expected 2)\n");
        return 1;
    }

    int deposit_amount = atoi(argv[1]);
    int withdraw_amount = atoi(argv[2]);
    pthread_t tids[NUM_THREADS];

    if (pthread_mutex_init(&mut, NULL) != 0)
    {
        printf("error (pthread_mutex_init)\n");
        return 1;
    }

    for (int i = 0; i < NUM_DEPOSIT; i++)
    {
        if (pthread_create(&tids[i], NULL, deposit, (void *)&deposit_amount) != 0)
        {
            printf("error (pthread_create)\n");
            return 1;
        }
    }

    for (int i = NUM_DEPOSIT; i < NUM_THREADS; i++)
    {
        if (pthread_create(&tids[i], NULL, withdraw, (void *)&withdraw_amount) != 0)
        {
            printf("error (pthread_create)\n");
            return 1;
        }
    }

    for (int i = 0; i < NUM_THREADS; i++)
    {
        if (pthread_join(tids[i], NULL) != 0)
        {
            printf("error (pthread_join)\n");
            return 1;
        }
    }

    printf("Final amount = %d\n", amount);
    return 0;
}
