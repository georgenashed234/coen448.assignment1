package coen448.computablefuture.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class AsyncProcessor {
    
    public CompletableFuture<String> processAsync(List<Microservice> microservices, String message) {
        
        List<CompletableFuture<String>> futures = microservices.stream()
            .map(client -> client.retrieveAsync(message))
            .collect(Collectors.toList());
        
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.joining(" ")));
        
    }
    
    public CompletableFuture<List<String>> processAsyncCompletionOrder(
            List<Microservice> microservices, String message) {

        List<String> completionOrder =
            Collections.synchronizedList(new ArrayList<>());

        List<CompletableFuture<Void>> futures = microservices.stream()
            .map(ms -> ms.retrieveAsync(message)
                .thenAccept(completionOrder::add))
            .collect(Collectors.toList());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> completionOrder);
        
    }
    
    public CompletableFuture<String> processAsyncFailFast(List<Microservice> services, List<String> messages) {
       //Error handler to see if there are no services.
        if (services == null || services.isEmpty()) return CompletableFuture.completedFuture("");
        

        List<CompletableFuture<String>> futures = new ArrayList<>();
        for (int i = 0; i < services.size(); i++) {//Going through all the services
            Microservice s = services.get(i);
            //Condition to make sure the messages list exist and the index i is valid.
            if(messages != null && i < messages.size()) {
                futures.add(s.retrieveAsync(messages.get(i)));
            } else {
                futures.add(s.retrieveAsync(null));
            }
        }

        // After all are done, combine the results.
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join) 
                        .collect(Collectors.joining(" ")));
    }

    public CompletableFuture<List<String>> processAsyncFailPartial( List<Microservice> services, List<String> messages) {
        //Error handler to see if there are no services.
        if (services == null || services.isEmpty()) return CompletableFuture.completedFuture(Collections.emptyList());
        

        List<CompletableFuture<String>> cf = new ArrayList<>();
        //looping through all the services and checking if the messages list
        //exist and the index i is valid, if not it will pass null to the retrieveAsync method.
        for (int i = 0; i < services.size(); i++) {
            String m = null; //placeholder

            if (i < messages.size() && messages != null) {
                m = messages.get(i);
            }

            CompletableFuture<String> future = services.get(i).retrieveAsync(m)
                .handle((res, e) -> {
                    if (e == null) { //if there is no exception, return the result
                        return res;
                    } else {
                        return null; //otherwise return null
                    }
                });

            cf.add(future); //add the future to the list
        }

        // After all aredone, filter out the failed ones and collect the successful results.
        // This will make sure only the successful results will be returned.
        return CompletableFuture.allOf(cf.toArray(new CompletableFuture[0]))
                .thenApply(v -> cf.stream()
                        .map(CompletableFuture::join)
                        .filter(x -> x != null) // filter out the failed ones
                        .collect(Collectors.toList()));
    }

    public CompletableFuture<String> processAsyncFailSoft( List<Microservice> services, List<String> messages, String fallbackValue) {
        //Error handler to see if there are no services. 
        // If so, it will return the fallback value or an empty string if the fallback value is null.
        if (services == null || services.isEmpty()) return CompletableFuture.completedFuture(fallbackValue == null ? "" : fallbackValue);
        

        List<CompletableFuture<String>> replaced = new ArrayList<>();
        for (int i = 0; i < services.size(); i++) {
            Microservice svc = services.get(i);
            String m = null; //placeholder

            if(messages != null && i < messages.size()) {
                m = messages.get(i);
            } else {
                m = null;
            }
            
            CompletableFuture<String> hf = svc.retrieveAsync(m)
                .handle((r, ex) -> {
                    if (ex == null) { //if null, return the result
                        return r;
                    } else { //otherwise return the fallback value or an empty string if the fallback value is null
                        if (fallbackValue == null) {
                            return "";
                        } else {
                            return fallbackValue;
                        }
                    }
                });

            replaced.add(hf);
        }
        // After all are done, combine the results.
        return CompletableFuture.allOf(replaced.toArray(new CompletableFuture[0]))
                .thenApply(v -> replaced.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.joining(" ")));
    }
    
}