# Collator
Android / JAVA(If you strip the annotations)

Currently in order to collect multi parallel async process/task results, there are a few ways to
do it.

1. `java.util.concurrent.Executor`
2. ???

However for simple libraries/examples, not many of them come with `java.util.concurrent.Future` or
`java.util.concurrent.CompletableFuture` which will allow you to collect the results with ease.

It is required to either make heavy changes or create some hackish ways to go around this.

This Collator library was created to reduce the need to make large changes to your existing
codes/libraries and to remove the need to know the number of results/completion to await for.

## Collators

#### CompletionCollator

Used to wait for multiple process to end before triggering a callback

E.g.
```
//Create the collator
CompletionCollator collator = new CompletionCollator();

if(//Some reason){
    //Reserve node before the start of a task
    final CompletionNode node = collator.reserveCompletion();

    AsyncTask<Void, Void, Void> longDelayTask = new AsyncTask<Void, Void, Void>() {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Log.e("longDelayTask", "Interrupted", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            node.completed();
        }
    };
}

if(//Some Other Reason){
    //Reserve node before the start of a task
    final CompletionNode node = collator.reserveCompletion();

    AsyncTask<Void, Void, Void> anotherLongDelayTask = new AsyncTask<Void, Void, Void>() {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Log.e("anotherLongDelayTask", "Interrupted", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            node.completed();
        }
    };
}


collator.awaitCompletion(new OnCompletionCallback() {
     @Override
     public void onComplete() {
         //Perform other codes after completion of the two AsyncTask
     }
});

```

#### CallbackCollator

Used to collect results from multiple async processes

E.g.
```
//Create the collator for String results
CallbackCollator<String> collator = new CallbackCollator<>();

if(//Some reason){
    //Reserve node before the start of a task
    final CallbackCollatorNode<String> node = collator.reserveCallback();

    AsyncTask<Void, Void, Void> longDelayTask = new AsyncTask<Void, Void, String>() {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Log.e("longDelayTask", "Interrupted", e);
            }
            return null;
        }

        @Override
        protected String onPostExecute(Void aVoid) {
            node.returnCallbackResult("Hello");
        }
    };
}

if(//Some Other Reason){
    //Reserve node before the start of a task
    final CallbackCollatorNode<String> node = collator.reserveCallback();

    AsyncTask<Void, Void, Void> anotherLongDelayTask = new AsyncTask<Void, Void, String>() {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Log.e("anotherLongDelayTask", "Interrupted", e);
            }
            return null;
        }

        @Override
        protected String onPostExecute(Void aVoid) {
            node.returnCallbackResult("World");
        }
    };
}


collator.awaitCallbacks(new CollatedResultsCallback<String>() {
     @Override
     public void onCompleteCallbacks(@NonNull List<String> collatedResults) {
         //Perform other codes after completion of the two AsyncTask
         //collatedResults will contain ["World", "Hello"] instead of ["Hello", "World"]
     }
});

```

## Q &amp; A

**What are the possible pitfalls of using this?**
>
1. The collators do not timeout. Hence if you forget to use the node, it will be stuck.
Timeout was not added as most async process/task should already have their own
timeout feature implemented, it does not make sense to add another timeout on top of those.
2. CallbackCollator does not collate results in sequence with the nodes created.
3. CallbackCollator does not accept `NULL` as callback result


**Why doesn't CallbackCollator accept `NULL` result?**

>If you are looking to differentiate success or failure, it might be better to use `Boolean`
for CallbackCollator type.

**I wish to collate some MODEL object result, how can I do so if it does not accept `NULL`?**

>Create a separate MODEL class that will store a `Boolean` and a @NULLABLE MODEL
object of your choice


**Do you have a Collator to collect multi return types?**

>No. But CallbackCollator should still be used with a MODEL class that can represent
the multi return types.

>
1. You can use their BASE class with a TYPE int for identification. Cast them according
to their TYPE.
2. You could create a class with various @NULLABLE variables of the same type.
(Workable but not recomended)