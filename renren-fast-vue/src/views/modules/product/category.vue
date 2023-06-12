<template>
    <div>
        <el-switch v-model="draggable" active-text="开启拖拽" inactive-text="关闭拖拽"></el-switch>
        <el-button v-if="draggable" @click="batchSave">批量保存</el-button>
        <el-button type="danger" @click="batchDelete">批量删除</el-button>
        <el-tree :data="menus" :props="defaultProps" @node-click="handleNodeClick" :expand-on-click-node="false"
            show-checkbox node-key="catId" :default-expanded-keys="expandedKey" :draggable="draggable"
            :allow-drop="allowDrop" ref="menuTree" @node-drop="handleDrop">
            <span class="custom-tree-node" slot-scope="{ node, data }">
                <span>{{ node.label }}</span>
                <span>
                    <!-- node.leve可以 看到自己当前是几级节点 如果它是一级或者二级节点才会显示出来 才能添加,三级字节点后就没有了-->
                    <el-button v-if="node.level <= 2" type="text" size="mini" @click="() => append(data)">
                        Append
                    </el-button>
                    <el-button type="text" size="mini" @click="() => edit(data)">
                        edit
                    </el-button>
                    <!--这里判断它的当前节点为0的话 就是说他没有子节点了,才能删除 -->
                    <el-button v-if="node.childNodes.length == 0" type="text" size="mini" @click="() => remove(node, data)">
                        Delete
                    </el-button>
                </span>
            </span>
        </el-tree>
        <el-dialog :title="title" :visible.sync="dialogVisible" width="30%">
            <el-form :model="category">
                <el-form-item label="分类名称">
                    <el-input v-model="category.name" autocomplete="off"></el-input>
                </el-form-item>
                <el-form-item label="图标">
                    <el-input v-model="category.icon" autocomplete="off"></el-input>
                </el-form-item>
                <el-form-item label="计量单位">
                    <el-input v-model="category.productUnit" autocomplete="off"></el-input>
                </el-form-item>
            </el-form>
            <span slot="footer" class="dialog-footer">
                <el-button @click="dialogVisible = false">取 消</el-button>
                <el-button type="primary" @click="submitData">确 定</el-button>
            </span>
        </el-dialog>
    </div>
</template>

<script>
//这里可以导入其他文件(比如：组件,工具js，第三方插件js，json文件，图片文件等等)
//例如:import《组件名称》 from '《组件路径》'；
export default {
    //import引入组件需要注入到对象中 才能使用
    components: {},
    props: {},

    //计算属性 类似于data概念
    computed: {},
    //监控data中的数据变化
    watch: {},
    data() {
        return {
            pCid: [],
            draggable: false,
            updateNodes: [],
            maxLevel: 1,
            title: '',
            dialogType: '',//edit,add
            category: {
                name: '',
                parentCid: 0,
                catLevel: 0,
                showStatus: 1,
                sort: 0,
                productUnit: "",
                icon: "",
                catId: null
            },
            dialogVisible: false,
            menus: [],  //返回所有分类数据
            expandedKey: [],//当删除新增,默认展开的位置
            defaultProps: {
                children: 'children',//指定子节点的属性
                label: 'name'//指定子节点的显示值
            }
        };
    },
    methods: {
        //批量删除
        batchDelete() {
            let catIds = [];
            let checkedNodes = this.$refs.menuTree.getCheckedNodes(); //refs拿到vue的所有组件
            console.log("批量删除，选中的元素", checkedNodes)
            for (let i = 0; i < checkedNodes.length; i++) {
                catIds.push(checkedNodes[i].catId);
            }
            this.$confirm(`是否批量删除【${catIds}】菜单`, '提示', {
                confirmButtonText: '确定',
                cancelButtonText: "取消",
                type: "warning"
            }).then(() => {
                this.$http({
                    url: this.$http.adornUrl("/product/category/delete"),
                    method: "post",
                    data: this.$http.adornData(catIds, false)
                }).then(({ data }) => {
                    this.$message({
                   message: "菜单批量删除成功",
                   type: "success"
                    });
                    this.getMenus();
                });
            }).catch(() => {

            });
        },
        //点击保存后，才保存当前拖拽信息的状态
        batchSave() {
            this.$http({
                url: this.$http.adornUrl("/product/category/update/sort"),
                method: "post",
                data: this.$http.adornData(this.updateNodes, false)
            }).then(({ data }) => {
                this.$message({
                    message: "菜单顺序等修改成功",
                    type: "success"
                });
                //刷新出新的菜单
                this.getMenus();
                //设置需要默认展开的菜单
                this.expandedKey = this.pCid;
                this.updateNodes = [];
                this.maxLevel = 0;
                // this.pCid=0;
            });
        },
        //拖拽事件成功后的事件
        handleDrop(draggingNode, dropNode, dropType, ev) {
            console.log("handleDrop", draggingNode, dropNode, dropType);
            //1.当前节点最新的父节点id
            let pCid = 0;
            let siblings = null;

            if (dropType == 'before' || dropType == "after") { //这个就是根据拖拽的方式来判断 before就是拖拽到指定位置差不多。
                console.log('dropNode', dropNode);
                pCid = dropNode.parent.data.catId == undefined ? 0 : dropNode.parent.data.catId;
                siblings = dropNode.parent.childNodes;
            } else {
                pCid = dropNode.data.catId;
                siblings = dropNode.childNodes;
            }
            this.pCid.push(pCid);
            //2.当前拖拽节点的最新顺序
            for (let i = 0; i < siblings.length; i++) {
                if (siblings[i].data.catId == draggingNode.data.catId) {
                    //如果遍历的是当前正在拖拽的节点
                    let catLevel = draggingNode.level;
                    if (siblings[i].level != draggingNode.level) {
                        //当前节点的层级发生变化
                        catLevel = siblings[i].level;
                        //修改它子节点的层级
                        this.updateChildNodeLevel(siblings[i]);
                    }
                    this.updateNodes.push({ catId: siblings[i].data.catId, sort: i, parentCid: pCid, catLevel: catLevel });
                } else {
                    //这里是以前节点的数据我们排序一下就ok
                    this.updateNodes.push({ catId: siblings[i].data.catId, sort: i });
                }
            }

            //3.当前拖拽节点的最新层级
            console.log("updateNodes", this.updateNodes);

        },

        updateChildNodeLevel(node) {
            console.log(node)
            if (node.childNodes.length > 0) {
                for (let i = 0; i < node.childNodes.length; i++) {
                    var cNode = node.childNodes[i].data;
                    this.updateNodes.push({ catId: cNode.catId, catLevel: node.childNodes[i].level });
                    this.updateChildNodeLevel(node.childNodes[i]);
                }
            }
        },
        //节点拖拽,判断
        allowDrop(draggingNode, dropNode, type) {
            console.log("节点拖拽属性", draggingNode, dropNode, type);
            //1.被拖动的当前节点以及所在的父节点总数不能大于3

            //1.被拖动的当前节点总层数

            //
            this.countNodeLevel(draggingNode);
            //当前正在拖动的节点+父节点所在的深度不大于3即可
            //计算深度
            console.log("maxLevel", this.maxLevel);
            console.log(draggingNode.data.catLevel)
            console.log(draggingNode.level)
            let deep = this.maxLevel - draggingNode.level + 1;
            console.log("节点深度:", deep);

            if (type == "inner") {
                console.log("节点深度if", deep + dropNode.level);
                return deep + dropNode.level <= 3;
            } else {
                console.log("节点深度else", deep + dropNode.parent.level)
                return deep + dropNode.parent.level <= 3;
            }
        },
        //统计当前和加起来 的节点数
        countNodeLevel(node) {
            //找到所有子节点，求出最大深度
            console.log("node", node)
            if (node.childNodes != null && node.childNodes.length > 0) {
                for (let i = 0; i < node.childNodes.length; i++) {
                    if (node.childNodes[i].level > this.maxLevel) {
                        this.maxLevel = node.childNodes[i].level;
                    }
                    this.countNodeLevel(node.childNodes[i])
                }

            }
        },
        handleNodeClick(data) {
            console.log(data);
        },
        getMenus() {
            this.$http({
                url: this.$http.adornUrl('/product/category/list/tree'),
                method: 'get',

            }).then(({ data }) => {
                console.log("成功获取到菜单数据...", data.data)
                this.menus = data.data
            })
        },
        //打开修改三级分类窗口
        edit(data) {
            console.log("要修改的数据", data)
            this.dialogType = "edit";
            this.title = "修改分类";
            this.dialogVisible = true;
            //发送请求获取当前节点最新的数据
            this.$http({
                url: this.$http.adornUrl(`/product/category/info/${data.catId}`),
                method: 'get',
            }).then(({ data }) => {
                //请求成功
                console.log("要回显的数据", data);
                this.category.name = data.category.name;
                this.category.catId = data.category.catId;
                this.category.icon = data.category.icon;
                this.category.productUnit = data.category.productUnit;
                this.category.parentCid = data.category.parentCid;
                this.category.sort = data.category.sort;
                this.category.showStatus = data.category.showStatus;
            })

        },
        //打开添加三级分类窗口
        append(data) {
            console.log("append", data);
            this.dialogType = "add";
            this.title = "添加分类"
            this.dialogVisible = true;
            this.category.parentCid = data.catId;
            this.category.catLevel = data.catLevel * 1 + 1;//怕是字符串强转一下 
            this.category.name = "";
            this.category.catId = null;
            this.category.icon = "";
            this.category.productUnit = "";
            this.category.sort = 0;
            this.category.showStatus = 1;
        },


        submitData() {
            if (this.dialogType == "add") {
                this.addCategory();
            }
            if (this.dialogType == "edit") {
                this.editCategory();
            }
        },
        //修改三级分类
        editCategory() {
            var { catId, name, icon, productUnit } = this.category;

            console.log({ catId, name, icon, productUnit })
            this.$http({
                url: this.$http.adornUrl("/product/category/update"),
                method: "post",
                data: this.$http.adornData({ catId, name, icon, productUnit }, false)
            }).then(({ data }) => {
                this.$message({
                    message: "菜单保存成功",
                    type: "success"
                });
                //关闭对话框
                this.dialogVisible = false;
                //刷新出新的菜单
                this.getMenus();
                //设置需要默认展开的菜单
                this.expandedKey = [this.category.parentCid];
            })
        },
        //添加三级分类
        addCategory() {
            console.log("添加三级分类的信息", this.category)
            this.$http({
                url: this.$http.adornUrl("/product/category/save"),
                method: "post",
                data: this.$http.adornData(this.category, false)
            }).then(({ data }) => {
                this.$message({
                    message: "菜单保存成功",
                    type: "success"
                })
                //关闭对话框
                this.dialogVisible = false;
                //刷新出新的菜单
                this.getMenus();
                //设置需要默认展开的菜单
                this.expandedKey = [this.category.parentCid];
            });
        },
        //删除三级分类
        remove(node, data) {
            var ids = [data.catId]
            this.$confirm(`是否删除该菜单${data.name}`, '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                this.$http({
                    url: this.$http.adornUrl("/product/category/delete"),
                    method: "post",
                    data: this.$http.adornData(ids, false)
                }).then(({ data }) => {
                    this.$message({
                        type: 'success',
                        message: '删除成功!'
                    });
                    // 调用方法,刷新菜单
                    this.getMenus();
                    //设置需要默认展开的菜单
                    this.expandedKey = [node.parent.data.catId]
                });

            }).catch(() => {
                this.$message({
                    type: 'info',
                    message: '已取消删除'
                });
            });



            console.log("remove", node, data)
        },
    },

    //生命周期-创建完成(可以访问当前this实例)
    created() {
        this.getMenus()
    },
    //生命周期- 挂载完成(可以访问DOM元素)
    mounted() { },
    beforeCreate() { },//生命周期-创建之间
    beforeMount() { }, //生命周期 -挂载之前
    beforeUpdate() { }, //生命周期 - 更新之前
    updated() { },//生命周期 - 更新之后
    beforeDestroy() { },//生命周期 - 销毁之前
    destrooyed() { },//生命周期 - 销毁完成
    activated() { } //如果页面有kepp-alive缓存功能，这个函数会触发 
}
</script>

<style lang="scss" scoped>
//@import url(); 引入公共css类
</style>