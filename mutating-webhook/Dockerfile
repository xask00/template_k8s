FROM openjdk:11
ADD build/distributions/*.tar /app

ENTRYPOINT ["/app/webhook/bin/webhook"]