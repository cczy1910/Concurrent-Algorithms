# MCS Lock
Реализация честной (First Come, First Served) блокировки основанная на флгортиме MCS, которая усыпляет
ждущий поток через `park` и пробуждает его через `unpark`. Вместо того, чтобы крутиться в бесконечном цикле, 
ожидая пока взявший блокировку поток её освободит.
