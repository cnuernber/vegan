# Which image to start from
FROM ubuntu:latest
MAINTAINER ChrisNuernberger

RUN apt-get -qq update && apt-get -qq -y install curl wget bzip2 nodejs npm \
    && apt-get -qq -y install build-essential libcairo2-dev libpango1.0-dev libjpeg-dev libgif-dev librsvg2-dev \
    && npm i canvas \
    && apt-get -qq -y autoremove \
    && apt-get autoclean \
    && rm -rf /var/lib/apt/lists/* /var/log/dpkg.log



COPY prod /prod
WORKDIR /prod

RUN cd /prod && rm -rf node_modules && npm link

ENTRYPOINT ["./app.js"]
