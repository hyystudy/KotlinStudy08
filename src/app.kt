import java.beans.PropertyChangeListener
import java.beans.PropertyChangeSupport
import kotlin.properties.Delegates
import kotlin.reflect.KProperty

fun main(args: Array<String>) {
    val p = Person("Alice")
    p.emails
    p.emails

    val p2 = PersonTwo("hyy")
    p2.emails
    p2.emails

    val worker = Worker("hyy", 25, 13500)
    worker.addPropertyChangeListener(
            PropertyChangeListener { event ->
                println("Property ${event.propertyName} changed " +
                        "from ${event.oldValue} to ${event.newValue}")
            }
    )

    worker.age =26
    worker.salary = 18000

    val worker1 = WorkerTwo("hyy", 25, 13500)
    worker1.addPropertyChangeListener(
            PropertyChangeListener { event ->
                println("Property ${event.propertyName} changed " +
                        "from ${event.oldValue} to ${event.newValue}")
            }
    )

    worker1.age =26
    worker1.salary = 18000

    val worker2 = WorkerThree("hyy", 25, 13500)
    worker2.addPropertyChangeListener(
            PropertyChangeListener { event ->
                println("Property ${event.propertyName} changed " +
                        "from ${event.oldValue} to ${event.newValue}")
            }
    )

    worker2.age =27
    worker2.salary = 20000

    val worker3 = WorkerFour("hyy", 25, 13500)
//    worker3.addPropertyChangeListener(
//            PropertyChangeListener { event ->
//                println("Property ${event.propertyName} changed " +
//                        "from ${event.oldValue} to ${event.newValue}")
//            }
//    )

    worker3.age = 27
    worker3.salary = 20000

    val s = Student()
    val data = mapOf("name" to "hyy", "company" to "Jccy")
    for ((key, value) in data){
        s.setAttributes(key, value)
    }

    println(s.name)
}

//使用支持属性来实现惰性初始化
class Email(val content: String)

class Person(val name: String) {
    private var _emails: List<Email>? = null

    val emails: List<Email>
        get() {
            if (_emails == null){
                _emails = loadEmails(this)
            }
            return _emails!!
        }


}

//使用委托属性实现惰性初始化 等用到的时候才会初始化
class PersonTwo(val name: String){
    val emails: List<Email> by lazy { loadEmails(this) }
}

fun loadEmails(person: Person): List<Email>{
    println("Load email for ${person.name}")
    return listOf(Email("hahah"), Email("qingjia"))
}

fun loadEmails(person: PersonTwo): List<Email>{
    println("Load email for ${person.name}")
    return listOf(Email("hahah"), Email("qingjia"))
}


//创建一个worker name可读 age salary可写
//用changeSupport 监听 age 和 salary 的变化
class Worker(val name: String, age: Int, salary: Int) : PropertyChangeAware(){
    var age: Int = age
        set(value) {
            val olderValue = field
            field = value
            changeSupport.firePropertyChange("age", olderValue, value)
        }

    var salary: Int = salary
        set(value) {
            val olderSalary = field
            field = value
            changeSupport.firePropertyChange("salary", olderSalary, value)
        }
}

//work 改进版 封装一个类 用于存储属性并发起通知
class WorkerTwo(val name: String, age: Int, salary: Int
) : PropertyChangeAware() {

    val _age = ObservableProperty("age", age, changeSupport)
    var age: Int
        get() = _age.getValue()
    set(value) { _age.setValue(value) }

    val _salary = ObservableProperty("salary", salary, changeSupport)
    var salary: Int
    get() = _salary.getValue()
    set(value) { _salary.setValue(value) }
}

class ObservableProperty(
        val propName: String, var propValue: Int,
        val changeSupport: PropertyChangeSupport
) {
    fun getValue(): Int = propValue

    fun setValue(value: Int) {
        val oldValue = propValue
        propValue = value
        changeSupport.firePropertyChange(propName, oldValue, value)
    }
}

//再用kotlin 改写
//work 改进版 封装一个类 用于存储属性并发起通知
class WorkerThree(val name: String, age: Int, salary: Int
) : PropertyChangeAware() {

   var age: Int by ObservablePropertyKotlin(age, changeSupport)
   var salary: Int by ObservablePropertyKotlin(salary, changeSupport)
}

class ObservablePropertyKotlin(
        var propValue: Int,
        val changeSupport: PropertyChangeSupport
) {
    operator fun getValue(workerThree: WorkerThree, prop: KProperty<*>): Int = propValue

    operator fun setValue(workerThree: WorkerThree, prop: KProperty<*>, value: Int) {
        val oldValue = propValue
        propValue = value
        changeSupport.firePropertyChange(prop.name, oldValue, value)
    }
}

//使用kotlin标准库
class WorkerFour(val name: String, age: Int, salary: Int
) : PropertyChangeAware() {

    private val observer = {
        prop: KProperty<*>, oldValue: Int, newValue: Int ->
        println("Property $prop.name changed " +
                "from $oldValue to $newValue")
        changeSupport.firePropertyChange(prop.name, oldValue, newValue)
    }

    var age: Int by Delegates.observable(age, observer)
    var salary: Int by Delegates.observable(salary, observer)
}

//使用map保存属性值
class Student {
    private val _attributes = hashMapOf<String, String>()

    fun setAttributes(attrName: String, value: String) {
        _attributes[attrName] = value
    }

    val name: String by _attributes
    //get() = _attributes["name"]!!
}