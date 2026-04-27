ARG VERSION
ARG VERSION_ALPINE=3.19
ARG VERSION_FABRIC=2.5.7

FROM hyperledger/fabric-tools:${VERSION_FABRIC} AS builder

ARG VERSION

WORKDIR /go/src/chaincode-ex02

COPY ./chaincode ./chaincode

RUN go mod init github.com/komune-io/c2/c2-chaincode/chaincode-ex02 &&\
    go get github.com/golang/protobuf@v1.5.3 &&\
    go get google.golang.org/grpc@v1.61.0 &&\
    go get github.com/hyperledger/fabric-chaincode-go@v0.0.0-20220920210243-7bc6fa0dd58b &&\
    go get github.com/hyperledger/fabric-protos-go@v0.2.0 &&\
    go mod vendor

RUN peer chaincode package -n ex02 -p chaincode/go/ex02 -v ${VERSION} ex02-${VERSION}.pak

FROM alpine:${VERSION_ALPINE}

ARG VERSION

WORKDIR /opt/chaincode/ex02

LABEL org.opencontainers.image.source="https://github.com/komune-io/connect-ssm"

RUN echo "CHAINCODE=ex02" > env_ex02 \
    && echo VERSION=${VERSION} >> env_ex02

COPY ./chaincode ./chaincode

COPY --from=builder /go/src/chaincode-ex02/ex02-${VERSION}.pak ./
