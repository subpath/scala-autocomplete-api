FROM hseeberger/scala-sbt:graalvm-ce-21.2.0-java8_1.6.2_3.1.1

WORKDIR /app
COPY . /app
RUN sbt stage
EXPOSE 3030

ENTRYPOINT ["./target/universal/stage/bin/scala-autocomplete-api"]
