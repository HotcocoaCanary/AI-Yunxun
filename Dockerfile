FROM ubuntu:latest
LABEL authors="Canary"

ENTRYPOINT ["top", "-b"]