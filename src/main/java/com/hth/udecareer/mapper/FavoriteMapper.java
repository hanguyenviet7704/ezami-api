package com.hth.udecareer.mapper;

import com.hth.udecareer.entities.FavoriteEntity;
import com.hth.udecareer.entities.FavoriteMetaEntity;
import com.hth.udecareer.model.response.CourseNoteResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FavoriteMapper {

//    @Mapping(target = "id", source = "favorite.id")
//    @Mapping(target = "userId", source = "favorite.userId")
//    @Mapping(target = "courseId", source = "favorite.favoritableId")
    @Mapping(target = "content", source = "favoriteMeta.metaValue")
    CourseNoteResponse toCourseNoteResponse(FavoriteEntity favorite, FavoriteMetaEntity favoriteMeta);
}