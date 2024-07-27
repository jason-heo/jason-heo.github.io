---
layout: post
title: "Using Python to Interact with HDFS Programmatically via PyArrow"
categories: "programming"
---

Recently, I needed to explore the HDFS file system using Python. After doing some research on Google, I discovered several libraries that could help. They all seemed promising, but I decided to go with PyArrow.

In this post, I'll explain how to use PyArrow to navigate the HDFS file system and then list some alternative options.

### 1) PyArrow

PyArrow integrates Hadoop jar files, which means that a JVM is required. This can be cumbersome for those who are not familiar with the JVM and Hadoop client settings.

However, this integration can be advantageous for those who are experienced with the JVM and Hadoop. For instance, you can leverage the existing `HADOOP_CONF_DIR` without any additional configuration. PyArrow also supports high availability (HA) for Namenode aliases like namenode1, which some other libraries do not support.

Additionally, connecting to a Kerberos-enabled HDFS server with a keytab is straightforward.

Here’s how to use PyArrow, assuming your HDFS server is secured with Kerberos.

- Setting Environmental Variables for PyArrow
  ```
  $ export HADOOP_HOME=<hadoop installed dir>
  $ export HADOOP_CONF_DIR=<hadoop conf dir>
  $ export ARROW_LIBHDFS_DIR=$HADOOP_HOME/lib/native/
  $ export CLASSPATH=$($HADOOP_HOME/bin/hdfs classpath --glob)
  ```
- Running `kinit` for authentication (cache file is important to connect secure hadoop)
  ```
  $ kinit --cache=/tmp/krb5cc_foo_bar \
      -kt /path/to/keytab \
      <your principal>
  ```
- installing pyarrow
  ```
  $ pip install pyarrow
  ```
- connecting to hdfs
  ```
  import pyarrow as pa
  import pyarrow.fs

  hdfs = pa.fs.HadoopFileSystem(
      host='namenode1',
      kerb_ticket='/tmp/krb5cc_foo_bar'
      port=8020,
      extra_conf=None      # Additional configuration
  )
  ```
- listing dir
  ```
  file_infos = hdfs.get_file_info(
      fs.FileSelector(base_dir='/path/to/dir', recursive=False)
  )
  ```
- what `file_infos` looks like
  ```
   [<FileInfo for '/path/to/dir/.Trash': type=FileType.Directory>
    ...
    <FileInfo for '/path/to/dir/warehouse': type=FileType.Directory>]"
  ```

Some online tutorials suggest using hdfs.connect() to establish a connection, but this function is deprecated.

For Mac Users:

You need to build `libhdfs.dylib` yourself

```
$ git clone https://github.com/apache/hadoop.git

$ git checkout rel/release-3.3.2

$ cd hadoop/hadoop-hdfs-project/hadoop-hdfs-native-client
$ mvn package \
    -Pdist,native \
    -DskipTests \
    -Dtar \
    -Dmaven.javadoc.skip=true
```

After `mvn packages` finished, `libhdfs.dylib` can be found in targets directory

### 2) Other Libraries

- https://hdfscli.readthedocs.io/en/latest/
  - This library looks promising and is actively maintained compared to others
- https://snakebite.readthedocs.io/en/latest/
  - Although it appeared to be a good choice, development has ceased, and it was archived in April 2022
- https://github.com/gupta-paras/pyhdfs_client
  - This is a native HDFS client designed for better performance. However, development stopped in 2021
