---
title: 'The beginnings of the API'
excerpt: ''
coverImage: '/assets/blog/dynamic-routing/cover.jpg'
date: '2022-12-11T06:00:00.000Z'
author:
name: Jacob Thompson
picture: '/assets/blog/authors/jj.jpeg'
ogImage:
url: '/assets/blog/dynamic-routing/cover.jpg'
---

# BirdSocial Introduction

The point of this project is foremost just to learn.
We don't plan on using this to make money or anything of the sort.
This is a parody that is meant as a joke and a learning project, any other interpretation of this shouldn't be considered.
That said, it should be quite clear that with the name **BirdSocial**, there is a target audience.

# API Introduction

This is where we begin writing the API, at this point the plan is to just *make it work* the best we can. We chose to write it in Kotlin as this would provide access to the neo4j

I started by following a [Spring Boot for GraphQL](https://docs.spring.io/spring-graphql/docs/current/reference/html/) tutorial. This got me up and running, [this article](https://www.baeldung.com/spring-graphql) by baeldung was also very useful, especially the [source code](https://github.com/eugenp/tutorials/tree/master/spring-boot-modules/spring-boot-graphql).

# Code Examples

## User
This is the first iteration of the data class that Spring Boot GraphQL is going to use for API returns.

It holds data and has no functions at this point.

```
data class User (
    val userId: UUID,
    val username: String,
    val displayName: String,
    val bio: String,
    val websiteUrl: String,
    val avatarUrl: String,
    val isVerified: Boolean,
    val chirpCount: Int,
    val followersCount: Int,
    val followingCount: Int,
)
```

## User Resolver
The `UserResolver` class is where all incoming GraphQL requests should be sent.

The class is annotated with `@Controller` to tell spring boot to send requests to this class. Then each function in the class is additionally annotated with `@QueryMapping` or `@MutationMapping` to search for this to resolve incoming queries.

```
@QueryMapping
fun users(): List<User> {
	return userDao.getUsers()
}
```

## User Args

This is another data class just like `User`, except this one is the data that a GraphQL query can populate.
The reason we can't just use the `User` that we already have is because we don't want to let people query for
sensitive things like passwords and emails.

## User Dao

This class is going to be replaced, but it stands in for a database.
The name stands for User Database Access Object, and it is as basic as possible. It's constructor takes in a `val users: List<User>`

## User Config

Lastly, we have the `UserConfig` class, this is annotated with `@Configuration`
because on Spring Boot Startup, this is going to run and setup any configuration items.
We have a `@Bean` called `userDao()` that is going to set up our `UserDao` with dummy data.