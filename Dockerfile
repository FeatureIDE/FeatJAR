FROM maven:3.8.6-openjdk-11
RUN apt-get update && apt-get install -y \
    git \
    build-essential \
    libgmp-dev
WORKDIR /home/FeatJAR
ENTRYPOINT ["make"]