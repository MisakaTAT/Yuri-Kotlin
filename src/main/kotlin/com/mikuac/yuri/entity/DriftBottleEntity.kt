package com.mikuac.yuri.entity

import org.hibernate.annotations.Type
import javax.persistence.*

@Entity
@Table(name = "driftbottle")
data class DriftBottleEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int,

    @Column(nullable = false)
    val groupId: Long,

    @Column(nullable = false)
    val groupName: String,

    @Column(nullable = false)
    val userId: Long,

    @Column(nullable = false)
    val userName: String,

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(nullable = false)
    val content: String,

    @Column(nullable = false)
    var open: Boolean = false,

    @Column(nullable = false)
    var openUser: Long = 0L,

    @Column(nullable = false)
    var openGroup: Long = 0L,

    @Column(nullable = false)
    var openUserName: String = "未知",

    @Column(nullable = false)
    var openGroupName: String = "未知"

)