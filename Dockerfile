FROM eclipse-temurin:17-jre

LABEL org.opencontainers.image.source=https://github.com/kleis-technology/cloud-assess
LABEL org.opencontainers.image.description="Cloud assess image"
LABEL org.opencontainers.image.licenses="MIT"
LABEL vendor1="Kleis Technology Sàrl"
LABEL vendor2="Resilio"

ARG LCAAC_VERSION=3.3.0

RUN apt-get update \
    && apt-get install -y --no-install-recommends unzip \
    && rm -rf /var/lib/apt/lists/*

ADD https://github.com/kleis-technology/lcaac/releases/download/v${LCAAC_VERSION}/lcaac-cli-v${LCAAC_VERSION}.tar.gz /tmp/lcaac.tar.gz
RUN tar -xzf /tmp/lcaac.tar.gz -C /opt \
    && rm /tmp/lcaac.tar.gz \
    && ln -s /opt/lcaac/bin/lcaac /usr/local/bin/lcaac

COPY trusted_library/lcaac.yaml /trusted_library/lcaac.yaml
COPY trusted_library/lib /trusted_library/lib
COPY trusted_library/inventory /trusted_library/inventory
COPY docker/entrypoint.sh /entrypoint.sh

ENV JAVA_OPTS="-Xmx4g"
WORKDIR /
EXPOSE 8080

ENTRYPOINT ["/entrypoint.sh"]
CMD ["--hide-locations"]
