FROM --platform=linux/amd64 openjdk:13

WORKDIR /usr/src/cfw-server/
COPY ./config config/
COPY ./resources/ resources/
COPY ./scripts/start.sh .
COPY ./scripts/stop.sh .

COPY ./target/lib lib/
COPY ./target/cfw-?.?.?.jar lib/

EXPOSE 8888

CMD ./start.sh




