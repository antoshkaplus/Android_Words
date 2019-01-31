
function guid() {
  function s4() {
    return Math.floor((1 + Math.random()) * 0x10000)
      .toString(16)
      .substring(1);
  }
  return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
    s4() + '-' + s4() + s4() + s4();
}

function ConvertWebTranslation(tr) {
    if (!tr.usages) {
        tr.usages = []
    }
    return tr
}

function Translation(foreignWord, nativeWord, kind) {
    this.foreignWord = foreignWord;
    this.nativeWord = nativeWord;
    this.kind = kind;
    this.deleted = false;
    this.usages = []
}

function Usage(usage) {
    this.usage = usage;
    this.creationDate = new Date();
}