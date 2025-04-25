#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>

#define LIST_LEN (20U)

typedef struct
{
    int lidx;
    int ridx;
} params_t;

int list[] = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20};
int sum = 0;

void *runSum(void *params_raw)
{
    params_t *params = (params_t *)params_raw;

    for (int i = params->lidx; i < params->ridx; i++)
    {
        sum += list[i];
    }

    pthread_exit(0);
}

int main(int argc, char **argv)
{
    pthread_t tid1, tid2;
    pthread_attr_t attr1, attr2;

    params_t param1 = {.lidx = 0, .ridx = LIST_LEN / 2};
    params_t param2 = {.lidx = LIST_LEN / 2, .ridx = LIST_LEN};

    pthread_attr_init(&attr1);
    pthread_attr_init(&attr2);

    pthread_create(&tid1, &attr1, runSum, &param1);
    pthread_create(&tid2, &attr2, runSum, &param2);

    pthread_join(tid1, NULL);
    pthread_join(tid2, NULL);

    printf("Sum of all numbers in the list is %d\n", sum);

    return 0;
}
