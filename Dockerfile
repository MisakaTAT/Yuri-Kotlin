# 编译
FROM gradle:8.0.2-jdk17 as BUILDER

MAINTAINER MisakaTAT<i@mikuac.com>

WORKDIR /yuri

COPY ./ .

RUN gradle clean bootJar --no-daemon

# 运行
FROM eclipse-temurin:17-jdk-centos7

MAINTAINER MisakaTAT<i@mikuac.com>

ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo '$TZ' > /etc/timezone

COPY --from=BUILDER /yuri/build/libs/yuri.jar /yuri/

WORKDIR /yuri

CMD ["nohup","java","-jar","yuri.jar","&"]
