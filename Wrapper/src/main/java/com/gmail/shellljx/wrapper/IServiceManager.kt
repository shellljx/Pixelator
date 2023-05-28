package com.gmail.shellljx.wrapper

interface IServiceManager {
    /**
     * 注册所有的业务service，如果没有注册的service在运行时 getService 将返回 null
     * @param businessServices 所有的业务 service
     */
    fun registerBusinessService(businessServices: List<Class<out IService>>)

    /**
     * 启动一个 service
     */
    fun <T : IService> startService(clazz: Class<T>)

    /**
     * 返回一个 service 实例，如果是业务 service 没有被注册将返回 null
     */
    fun <T : IService> getService(clazz: Class<T>): T?

    /**
     * 结束一个 service
     */
    fun <T : IService> stopService(clazz: Class<T>)

    fun destroy()
}
