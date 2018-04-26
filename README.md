# RowingStatistic

Мое первое полноценное коммерческое приложение. Заказчиком выступал директор компании [Biorow](http://biorow.com/).

Причина создания приложения была проблема обработки данных полученных после тренировки т.к. они выгружались с датчиков в виде .csv файлов, которые потом руками обрабатывались в экселе.  Поэтому стояла задача максимально автоматизировать эту обработку. В результате все свелось к тому, что плользователю надо загрузить файлы в приложение и указать параметры обработки. На выходе получался .csv файл с готовыми результатми (требование ТЗ).

### Как оно работает?
Приложение состоит из двух экранов. Первый это экран загрузки данных на котором можно:
* Загрузить файлы с данными (до 8 штук)
* Выбрать значение по оси абсцисс (время/дистанция)
* Переименовать график(и)

<a> <img src="https://user-images.githubusercontent.com/25584477/39307687-2dc002ac-496c-11e8-9ac4-05d69ec4122c.png"  height="300" width="533"> </a>

На втором экране проходит работа с данными.
На нем имеются кнопки с указанием начало и конца выборки, обработки данных и записи их в файл. Так же смены значений по оси абсцисс.

<a> <img src="https://user-images.githubusercontent.com/25584477/39307688-2de359aa-496c-11e8-87ae-e5ee64b2811d.png"  height="300" width="533"> </a>

На верхнем графике выводится скорость и темп гребли. В случае если было загружено более чем один файл, то значения на этом графике строятся по первому файлу. На нижем выводится темп гребли. На графиках можно увеличивать масштаб и выбирать значения, причем изменения на одном полноностью синхронизируются на втором.

<a> <img src="https://user-images.githubusercontent.com/25584477/39307689-2e06591e-496c-11e8-9685-a20a071357af.png"  height="300" width="533"> </a>
