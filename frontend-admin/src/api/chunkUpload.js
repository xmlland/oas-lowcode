import {postActionByParams, uploadAction} from "@/api/action";
import {uploadUrl} from "@/api/api";

const _chunkSize = 1024 * 1024 * 10 // 10M
// 负责将文件切片。
const cutFile = (file, chunkSize = _chunkSize) => {
    let pending = [],
        total = file.size,
        chunks = chunkSize ? Math.ceil(total / chunkSize) : 1,
        start = 0,
        index = 0,
        len, api;

    api = {
        file: file,

        has: function () {
            return !!pending.length;
        },

        shift: function () {
            return pending.shift();
        },

        unshift: function (block) {
            pending.unshift(block);
        }
    };

    while (index < chunks) {
        len = Math.min(chunkSize, total - start);
        let end = chunkSize ? (start + len) : total
        pending.push({
            file: file,
            start: start,
            end: end,
            total: total,
            chunks: chunks,
            chunk: index++,
            cuted: api,
            data: file.slice(start, end)
        });
        start += len;
    }

    file.blocks = pending.concat();
    file.remaning = pending.length;

    return api;
}
export default (upload, onProcessChange, id, oss, idPrefix) => {

    // console.log(upload)

    upload.file.id = id
    return new Promise((resolve, reject) => {
        let cutFile1 = cutFile(upload.file, _chunkSize);
        let chunk = 0;
        let actions = []
        let times = 0
        while (cutFile1.has()) {
            let cut = cutFile1.shift();
            let name = upload.file.name
            actions.push(new Promise((resolve1, reject1) => {
                uploadAction(uploadUrl, {
                    groupId: id,
                    chunk: chunk,
                    id: upload.file.uid,
                    name: upload.file.name,
                    type: upload.file.type,
                    lastModifiedDate: upload.file.lastModifiedDate,
                    size: upload.file.size,
                    oss: oss,
                    idPrefix: idPrefix,
                    file: new File([cut.data], name, {type: upload.file.type})
                }, (progressEvent) => {
                    // console.log(progressEvent)
                    let uploadProgress = progressEvent.loaded / progressEvent.total / cut.chunks
                    let totalComplete = times / cut.chunks + uploadProgress
                    onProcessChange(totalComplete * 100)
                }).then((res) => {
                    times++
                    resolve1(res)
                }).catch(err => {
                    reject1(err)
                })
            }))

            chunk++;
        }

        Promise.all(actions).then(chunkRes => {
            console.log('上传完成', chunkRes)

            postActionByParams(uploadUrl, {
                groupId: id,
                fileName: upload.file.name,
                fileSize: upload.file.size,
                chunk: chunk,
                isChunk: false,
                isEncoding: false,
                cSize: _chunkSize,
                oss: oss,
                idPrefix: idPrefix
            }).then(upRes => {
                resolve(upRes)
            })
        }).catch(err => {
            reject(err)
        })

    })
}
