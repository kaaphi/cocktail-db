FROM openjdk:11-slim

WORKDIR /cocktails

ADD ./build/distributions/cocktail-db-1.0.tar /cocktails

EXPOSE 7000

     
CMD ["./cocktail-db-1.0/bin/cocktail-db"]