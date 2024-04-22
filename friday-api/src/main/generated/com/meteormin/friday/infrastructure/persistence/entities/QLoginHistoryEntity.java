package com.meteormin.friday.infrastructure.persistence.entities;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QLoginHistoryEntity is a Querydsl query type for LoginHistoryEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QLoginHistoryEntity extends EntityPathBase<LoginHistoryEntity> {

    private static final long serialVersionUID = 1075082043L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QLoginHistoryEntity loginHistoryEntity = new QLoginHistoryEntity("loginHistoryEntity");

    public final com.meteormin.friday.infrastructure.persistence.QBaseEntity _super = new com.meteormin.friday.infrastructure.persistence.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final DateTimePath<java.time.LocalDateTime> deletedAt = createDateTime("deletedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath ip = createString("ip");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final QUserEntity user;

    public QLoginHistoryEntity(String variable) {
        this(LoginHistoryEntity.class, forVariable(variable), INITS);
    }

    public QLoginHistoryEntity(Path<? extends LoginHistoryEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QLoginHistoryEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QLoginHistoryEntity(PathMetadata metadata, PathInits inits) {
        this(LoginHistoryEntity.class, metadata, inits);
    }

    public QLoginHistoryEntity(Class<? extends LoginHistoryEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new QUserEntity(forProperty("user")) : null;
    }

}

