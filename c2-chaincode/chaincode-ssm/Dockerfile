ARG VERSION
ARG VERSION_ALPINE=3.19
ARG VERSION_FABRIC=2.5.7

FROM alpine:${VERSION_ALPINE} AS fetcher

WORKDIR /git

RUN apk update && \
	apk upgrade && \
	apk --no-cache add tar=1.35-r2 wget=1.21.4-r0 unzip=6.0-r14

RUN wget --no-check-certificate --progress=dot -O clone.zip https://github.com/apoupard/blockchain-ssm/archive/fabric-v2-images.zip && \
    unzip clone.zip && \
    mv blockchain-ssm-* blockchain-ssm

FROM hyperledger/fabric-tools:${VERSION_FABRIC} AS builder

ARG VERSION

WORKDIR /go/src/chaincode-ssm

COPY --from=fetcher /git/blockchain-ssm/chaincode /go/src/chaincode-ssm/chaincode

RUN go mod init github.com/komune-io/c2/c2-chaincode/chaincode-ssm &&\
    go get github.com/golang/protobuf@v1.5.3 &&\
    go get google.golang.org/grpc@v1.61.0 &&\
    go get github.com/hyperledger/fabric-chaincode-go@v0.0.0-20220920210243-7bc6fa0dd58b &&\
    go get github.com/hyperledger/fabric-protos-go@v0.2.0 &&\
    go mod vendor

RUN peer chaincode package -n ssm -p chaincode/go/ssm -v $VERSION ssm-$VERSION.pak

FROM alpine:${VERSION_ALPINE}

ARG VERSION

WORKDIR /opt/chaincode/ssm

LABEL org.opencontainers.image.source="https://github.com/komune-io/connect-ssm"

RUN echo "CHAINCODE=ssm" > env_ssm \
    && echo VERSION=$VERSION >> env_ssm

COPY --from=fetcher /git/blockchain-ssm/sdk/cli/util ./bin
COPY --from=fetcher /git/blockchain-ssm/chaincode ./chaincode
COPY --from=builder /go/src/chaincode-ssm/ssm-$VERSION.pak ./
