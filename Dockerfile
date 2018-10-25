
# Second stage is the runtime environment
FROM library/ubuntu
ENV  JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF8
USER root
RUN apt-get update && \
    apt-get install software-properties-common -y && \
    add-apt-repository ppa:webupd8team/java -y && \
    apt-get update && \
    echo "oracle-java8-installer shared/accepted-oracle-license-v1-1 select true" | debconf-set-selections && \
    apt-get install oracle-java8-installer oracle-java8-set-default -y
# Change to /root directory
RUN apt-get update && apt-get install -y \
    curl zip python git bzip2 build-essential

RUN curl -L -o /bin/bazel.sh https://github.com/bazelbuild/bazel/releases/download/0.17.1/bazel-0.17.1-installer-linux-x86_64.sh && \
    chmod +x /bin/bazel.sh && \
    /bin/bazel.sh && \
    rm /bin/bazel.sh
    
RUN mkdir -p /root/onos
WORKDIR /root/onos

# Install ONOS
#COPY --from=builder /src/tar/ .

# Configure ONOS to log to stdout
#RUN sed -ibak '/log4j.rootLogger=/s/$/, stdout/' $(ls -d apache-karaf-*)/etc/org.ops4j.pax.logging.cfg

LABEL org.label-schema.name="ONOS" \
      org.label-schema.description="SDN Controller" \
      org.label-schema.usage="http://wiki.onosproject.org" \
      org.label-schema.url="http://onosproject.org" \
      org.label-scheme.vendor="Open Networking Foundation" \
      org.label-schema.schema-version="1.0"

# Ports
# 6653 - OpenFlow
# 6640 - OVSDB
# 8181 - GUI
# 8101 - ONOS CLI
# 9876 - ONOS intra-cluster communication
EXPOSE 6653 6640 8181 8101 9876

# Get ready to run command
#ENTRYPOINT ["./bin/onos-service"]
#CMD ["server"]
