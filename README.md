# Collator
JAVA

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

### CompletionCollator

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



## What are the possible pitfalls of using this?

1. The collators do not timeout. Hence if you forget to use the node, it will be stuck.
Timeout was not added as most async process/task should already have their own
timeout feature implemented, it does not make sense to add another timeout on top of those.