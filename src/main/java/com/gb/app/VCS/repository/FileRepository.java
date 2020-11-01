package com.gb.app.VCS.repository;

import com.gb.app.VCS.models.FileInfo;
import org.springframework.data.couchbase.core.query.Query;
import org.springframework.data.couchbase.repository.CouchbaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileRepository extends CouchbaseRepository<FileInfo, String> {

    @Query("#{#n1ql.selectEntity} WHERE #{#n1ql.filter} AND meta().id like $1")
    List<FileInfo> findGetAllVersion(String documentIdRegex);
}
