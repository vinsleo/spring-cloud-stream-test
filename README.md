# spring-cloud-stream-test


- created app using initializr from https://start.spring.io/ -> version used 2.1.0.M2
- copied the kstream-binder intergation test from https://github.com/spring-cloud/spring-cloud-stream-binder-kafka/blob/master/spring-cloud-stream-binder-kafka-streams/src/test/java/org/springframework/cloud/stream/binder/kafka/streams/integration/KafkaStreamsBinderPojoInputAndPrimitiveTypeOutputTests.java 
- refactored it to separate out the class and properties and ran the test

heres the output of the test 
> ava.lang.ClassCastException: com.sun.proxy.$Proxy86 cannot be cast to org.springframework.messaging.MessageChannel
	at org.springframework.cloud.stream.test.binder.TestSupportBinder.bindProducer(TestSupportBinder.java:67) ~[spring-cloud-stream-test-support-2.1.0.BUILD-20180910.155157-128.jar:2.1.0.BUILD-SNAPSHOT]

to reproduce error 
- checkout this code 
- run ./mvnw clean test
