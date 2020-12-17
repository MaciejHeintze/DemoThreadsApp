# Demo Threads App (Coroutines)

Application focuses on doing asynchronous operations using Kotlin coroutines. 

After pressing Start button two async operations are launched: 
1. Getting device coordinates every X seconds and saving them as text to list 
2. Getting device battery level every X seconds and saving it to list

All operations are working asynchronusly. When list reaches specific size then coroutines operations are cancelled and list is transformed to signle
string which is send to http server (in fact I'm only simulating SEND operation just by printing message to the Log). 

When Stop button is pressed all operations are cancelled.
