#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define PROJECT_NAME "clox"


#define MAX_LENGTH 100

struct list_elt{
    char* value;
    struct list_elt* next;
    struct list_elt* prev;
};

typedef struct list_elt list_elt;

struct list {
    list_elt* first;
};
typedef struct list *list;

list_elt* new_elt(char value[])
{
    list_elt* l;
    l = malloc(sizeof(list_elt));
    l->next = NULL;
    l->prev = NULL;
    l->value = malloc(strlen(value)+1);
    strcpy(l->value, value);
    return l;
}

list new_list(void){
    list l = malloc(sizeof(struct list));
    l->first = NULL;
    return l;
}
void delete_list_elt(list_elt* elt){
    if(elt != NULL) {
        free(elt->value);
        if(elt->prev != NULL)
            elt->prev->next = NULL;
        if(elt->next != NULL)
            delete_list_elt(elt->next);
        free(elt);
    }
}
void delete_list(list l){
    if (l == NULL || l->first == NULL)
        return;
    delete_list_elt(l->first);
    l->first = NULL;
}
void free_list(list l){
    if(l->first != NULL)
        delete_list(l);
    free(l);
}
void delete_elt_elt(list_elt* l, list_elt* elt){
    if(l == NULL || elt == NULL)
        return;
    if (l == elt){
        if(l->prev != NULL)
            l->prev->next = l->next;
        if(l->next != NULL)
            l->next->prev = l->prev;
        free(l->value);
        free(l);
    }
    else
        delete_elt_elt(l->next, elt);
}
void delete_elt(list l, list_elt* elt){
    if (l != NULL)
        delete_elt_elt(l->first, elt);
}
list_elt* find_elt(list_elt* elt, char value[]){
    if ((elt == NULL) || (strcmp(elt->value, value) == 0))
        return elt;
    else
        return find_elt(elt->next, value);
}

list_elt* find(list l, char value[]){
    if(l == NULL)
        return NULL;
    return find_elt(l->first, value);
}
void insert_elt(list_elt* elt, char value[]){
    if(elt == NULL)
        return;

    while(elt->next != NULL)
        elt = elt->next;
    
    elt->next = new_elt(value);
    elt->next->prev = elt;
}
void insert(list l, char value[]){
    if (l == NULL)
        return;
    if(l->first == NULL)
        l->first = new_elt(value);
    else
        insert_elt(l->first, value);
}

void print_list_elt(list_elt* elt){
    if (elt == NULL)
        return;
    printf("|- %s\n",elt->value);
    print_list_elt(elt->next);
}
void print_list(list l){
    printf("---\n");
    if(l->first != NULL)
        print_list_elt(l->first);
    else
        printf("  nil\n");
    printf("---\n");
}
int main(void) {
    list l = new_list();

    insert(l,"salut");
    insert(l,"ca");
    insert(l,"va");
    insert(l,"?");
    print_list(l);

    list_elt* elt = find(l,"va");
    delete_elt(l, elt);
    print_list(l);

    elt = find(l,"schtroumpf");
    delete_elt(l, elt);
    print_list(l);

    delete_list(l);
    print_list(l);

    free_list(l);

    return 0;
}
