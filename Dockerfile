FROM gradle:8.2 as builder

ARG GITHUB_ACTOR
ARG GITHUB_TOKEN

RUN export GITHUB_ACTOR=${GITHUB_ACTOR}
RUN export GITHUB_TOKEN=${GITHUB_TOKEN}

WORKDIR /build/libs
ARG APPJAR=build/libs/*.jar
COPY ${APPJAR} app.jar
RUN jar -xf ./app.jar

FROM eclipse-temurin:17

LABEL org.opencontainers.image.source=https://github.com/kleis-technology/cloud-assess
LABEL org.opencontainers.image.description="Cloud assess image"
LABEL org.opencontainers.image.licenses="MIT"
LABEL vendor1="Resilio"
LABEL vendor2="Kleis Technology Sàrl"

ARG DEPENDENCY=/build/libs

COPY --from=builder ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=builder ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=builder ${DEPENDENCY}/BOOT-INF/classes /app

ENTRYPOINT ["java","-Xmx4g", "-cp","app:app/lib/*","org.cloud_assess.ApplicationKt"]
