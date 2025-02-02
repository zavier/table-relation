import{G as g}from"./graph-CJwPbK8k.js";import{bg as Te,aX as Me,bh as ce,aR as X,bi as Ie,aN as le,b0 as ee,aT as F,aO as he,bj as je,bk as Se,bl as Fe,aW as q,aV as Ve,bm as Be,bn as Ae,bo as Ye,bp as C,ax as w,au as m,bq as T,br as M,bs as Ge,bt as $}from"./index-9W6MuPV5.js";import{c as ve,a as f,f as R,v as x,r as I}from"./reduce-C7v7xba3.js";import{b as pe,a as De,t as V,m as L,h as we,f as z,d as qe}from"./min-Bz2wNbRv.js";function $e(e,n){return e==null?e:Te(e,ve(n),Me)}function We(e,n){return ce(e,ve(n))}function Xe(e,n){return e>n}function j(e,n){var r={};return n=X(n),ce(e,function(t,a,i){Ie(r,a,n(t,a,i))}),r}function y(e){return e&&e.length?pe(e,le,Xe):void 0}function U(e,n){return e&&e.length?pe(e,X(n),De):void 0}function ze(e,n){var r=e.length;for(e.sort(n);r--;)e[r]=e[r].value;return e}function Ue(e,n){if(e!==n){var r=e!==void 0,t=e===null,a=e===e,i=ee(e),o=n!==void 0,u=n===null,d=n===n,s=ee(n);if(!u&&!s&&!i&&e>n||i&&o&&d&&!u&&!s||t&&o&&d||!r&&d||!a)return 1;if(!t&&!i&&!s&&e<n||s&&r&&a&&!t&&!i||u&&r&&a||!o&&a||!d)return-1}return 0}function He(e,n,r){for(var t=-1,a=e.criteria,i=n.criteria,o=a.length,u=r.length;++t<o;){var d=Ue(a[t],i[t]);if(d){if(t>=u)return d;var s=r[t];return d*(s=="desc"?-1:1)}}return e.index-n.index}function Je(e,n,r){n.length?n=F(n,function(i){return he(i)?function(o){return je(o,i.length===1?i[0]:i)}:i}):n=[le];var t=-1;n=F(n,Se(X));var a=Fe(e,function(i,o,u){var d=F(n,function(s){return s(i)});return{criteria:d,index:++t,value:i}});return ze(a,function(i,o){return He(i,o,r)})}var Ze=Math.ceil,Ke=Math.max;function Qe(e,n,r,t){for(var a=-1,i=Ke(Ze((n-e)/(r||1)),0),o=Array(i);i--;)o[++a]=e,e+=r;return o}function en(e){return function(n,r,t){return t&&typeof t!="number"&&q(n,r,t)&&(r=t=void 0),n=V(n),r===void 0?(r=n,n=0):r=V(r),t=t===void 0?n<r?1:-1:V(t),Qe(n,r,t)}}var E=en(),_=Ve(function(e,n){if(e==null)return[];var r=n.length;return r>1&&q(e,n[0],n[1])?n=[]:r>2&&q(n[0],n[1],n[2])&&(n=[n[0]]),Je(e,Be(n,1),[])}),nn=0;function H(e){var n=++nn;return Ae(e)+n}function rn(e,n,r){for(var t=-1,a=e.length,i=n.length,o={};++t<a;){var u=t<i?n[t]:void 0;r(o,e[t],u)}return o}function tn(e,n){return rn(e||[],n||[],Ye)}class an{constructor(){var n={};n._next=n._prev=n,this._sentinel=n}dequeue(){var n=this._sentinel,r=n._prev;if(r!==n)return ne(r),r}enqueue(n){var r=this._sentinel;n._prev&&n._next&&ne(n),n._next=r._next,r._next._prev=n,r._next=n,n._prev=r}toString(){for(var n=[],r=this._sentinel,t=r._prev;t!==r;)n.push(JSON.stringify(t,on)),t=t._prev;return"["+n.join(", ")+"]"}}function ne(e){e._prev._next=e._next,e._next._prev=e._prev,delete e._next,delete e._prev}function on(e,n){if(e!=="_next"&&e!=="_prev")return n}function un(e,n){if(e.nodeCount()<=1)return[];var r=sn(e,n),t=dn(r.graph,r.buckets,r.zeroIdx);return C(w(t,function(a){return e.outEdges(a.v,a.w)}))}function dn(e,n,r){for(var t=[],a=n[n.length-1],i=n[0],o;e.nodeCount();){for(;o=i.dequeue();)B(e,n,r,o);for(;o=a.dequeue();)B(e,n,r,o);if(e.nodeCount()){for(var u=n.length-2;u>0;--u)if(o=n[u].dequeue(),o){t=t.concat(B(e,n,r,o,!0));break}}}return t}function B(e,n,r,t,a){var i=a?[]:void 0;return f(e.inEdges(t.v),function(o){var u=e.edge(o),d=e.node(o.v);a&&i.push({v:o.v,w:o.w}),d.out-=u,W(n,r,d)}),f(e.outEdges(t.v),function(o){var u=e.edge(o),d=o.w,s=e.node(d);s.in-=u,W(n,r,s)}),e.removeNode(t.v),i}function sn(e,n){var r=new g,t=0,a=0;f(e.nodes(),function(u){r.setNode(u,{v:u,in:0,out:0})}),f(e.edges(),function(u){var d=r.edge(u.v,u.w)||0,s=n(u),c=d+s;r.setEdge(u.v,u.w,c),a=Math.max(a,r.node(u.v).out+=s),t=Math.max(t,r.node(u.w).in+=s)});var i=E(a+t+3).map(function(){return new an}),o=t+1;return f(r.nodes(),function(u){W(i,o,r.node(u))}),{graph:r,buckets:i,zeroIdx:o}}function W(e,n,r){r.out?r.in?e[r.out-r.in+n].enqueue(r):e[e.length-1].enqueue(r):e[0].enqueue(r)}function fn(e){var n=e.graph().acyclicer==="greedy"?un(e,r(e)):cn(e);f(n,function(t){var a=e.edge(t);e.removeEdge(t),a.forwardName=t.name,a.reversed=!0,e.setEdge(t.w,t.v,a,H("rev"))});function r(t){return function(a){return t.edge(a).weight}}}function cn(e){var n=[],r={},t={};function a(i){Object.prototype.hasOwnProperty.call(t,i)||(t[i]=!0,r[i]=!0,f(e.outEdges(i),function(o){Object.prototype.hasOwnProperty.call(r,o.w)?n.push(o):a(o.w)}),delete r[i])}return f(e.nodes(),a),n}function ln(e){f(e.edges(),function(n){var r=e.edge(n);if(r.reversed){e.removeEdge(n);var t=r.forwardName;delete r.reversed,delete r.forwardName,e.setEdge(n.w,n.v,r,t)}})}function O(e,n,r,t){var a;do a=H(t);while(e.hasNode(a));return r.dummy=n,e.setNode(a,r),a}function hn(e){var n=new g().setGraph(e.graph());return f(e.nodes(),function(r){n.setNode(r,e.node(r))}),f(e.edges(),function(r){var t=n.edge(r.v,r.w)||{weight:0,minlen:1},a=e.edge(r);n.setEdge(r.v,r.w,{weight:t.weight+a.weight,minlen:Math.max(t.minlen,a.minlen)})}),n}function be(e){var n=new g({multigraph:e.isMultigraph()}).setGraph(e.graph());return f(e.nodes(),function(r){e.children(r).length||n.setNode(r,e.node(r))}),f(e.edges(),function(r){n.setEdge(r,e.edge(r))}),n}function re(e,n){var r=e.x,t=e.y,a=n.x-r,i=n.y-t,o=e.width/2,u=e.height/2;if(!a&&!i)throw new Error("Not possible to find intersection inside of the rectangle");var d,s;return Math.abs(i)*o>Math.abs(a)*u?(i<0&&(u=-u),d=u*a/i,s=u):(a<0&&(o=-o),d=o,s=o*i/a),{x:r+d,y:t+s}}function S(e){var n=w(E(me(e)+1),function(){return[]});return f(e.nodes(),function(r){var t=e.node(r),a=t.rank;m(a)||(n[a][t.order]=r)}),n}function vn(e){var n=L(w(e.nodes(),function(r){return e.node(r).rank}));f(e.nodes(),function(r){var t=e.node(r);we(t,"rank")&&(t.rank-=n)})}function pn(e){var n=L(w(e.nodes(),function(i){return e.node(i).rank})),r=[];f(e.nodes(),function(i){var o=e.node(i).rank-n;r[o]||(r[o]=[]),r[o].push(i)});var t=0,a=e.graph().nodeRankFactor;f(r,function(i,o){m(i)&&o%a!==0?--t:t&&f(i,function(u){e.node(u).rank+=t})})}function te(e,n,r,t){var a={width:0,height:0};return arguments.length>=4&&(a.rank=r,a.order=t),O(e,"border",a,n)}function me(e){return y(w(e.nodes(),function(n){var r=e.node(n).rank;if(!m(r))return r}))}function wn(e,n){var r={lhs:[],rhs:[]};return f(e,function(t){n(t)?r.lhs.push(t):r.rhs.push(t)}),r}function bn(e,n){return n()}function mn(e){function n(r){var t=e.children(r),a=e.node(r);if(t.length&&f(t,n),Object.prototype.hasOwnProperty.call(a,"minRank")){a.borderLeft=[],a.borderRight=[];for(var i=a.minRank,o=a.maxRank+1;i<o;++i)ae(e,"borderLeft","_bl",r,a,i),ae(e,"borderRight","_br",r,a,i)}}f(e.children(),n)}function ae(e,n,r,t,a,i){var o={width:0,height:0,rank:i,borderType:n},u=a[n][i-1],d=O(e,"border",o,r);a[n][i]=d,e.setParent(d,t),u&&e.setEdge(u,d,{weight:1})}function gn(e){var n=e.graph().rankdir.toLowerCase();(n==="lr"||n==="rl")&&ge(e)}function yn(e){var n=e.graph().rankdir.toLowerCase();(n==="bt"||n==="rl")&&kn(e),(n==="lr"||n==="rl")&&(xn(e),ge(e))}function ge(e){f(e.nodes(),function(n){ie(e.node(n))}),f(e.edges(),function(n){ie(e.edge(n))})}function ie(e){var n=e.width;e.width=e.height,e.height=n}function kn(e){f(e.nodes(),function(n){A(e.node(n))}),f(e.edges(),function(n){var r=e.edge(n);f(r.points,A),Object.prototype.hasOwnProperty.call(r,"y")&&A(r)})}function A(e){e.y=-e.y}function xn(e){f(e.nodes(),function(n){Y(e.node(n))}),f(e.edges(),function(n){var r=e.edge(n);f(r.points,Y),Object.prototype.hasOwnProperty.call(r,"x")&&Y(r)})}function Y(e){var n=e.x;e.x=e.y,e.y=n}function En(e){e.graph().dummyChains=[],f(e.edges(),function(n){On(e,n)})}function On(e,n){var r=n.v,t=e.node(r).rank,a=n.w,i=e.node(a).rank,o=n.name,u=e.edge(n),d=u.labelRank;if(i!==t+1){e.removeEdge(n);var s=void 0,c,l;for(l=0,++t;t<i;++l,++t)u.points=[],s={width:0,height:0,edgeLabel:u,edgeObj:n,rank:t},c=O(e,"edge",s,"_d"),t===d&&(s.width=u.width,s.height=u.height,s.dummy="edge-label",s.labelpos=u.labelpos),e.setEdge(r,c,{weight:u.weight},o),l===0&&e.graph().dummyChains.push(c),r=c;e.setEdge(r,a,{weight:u.weight},o)}}function Nn(e){f(e.graph().dummyChains,function(n){var r=e.node(n),t=r.edgeLabel,a;for(e.setEdge(r.edgeObj,t);r.dummy;)a=e.successors(n)[0],e.removeNode(n),t.points.push({x:r.x,y:r.y}),r.dummy==="edge-label"&&(t.x=r.x,t.y=r.y,t.width=r.width,t.height=r.height),n=a,r=e.node(n)})}function J(e){var n={};function r(t){var a=e.node(t);if(Object.prototype.hasOwnProperty.call(n,t))return a.rank;n[t]=!0;var i=L(w(e.outEdges(t),function(o){return r(o.w)-e.edge(o).minlen}));return(i===Number.POSITIVE_INFINITY||i===void 0||i===null)&&(i=0),a.rank=i}f(e.sources(),r)}function P(e,n){return e.node(n.w).rank-e.node(n.v).rank-e.edge(n).minlen}function ye(e){var n=new g({directed:!1}),r=e.nodes()[0],t=e.nodeCount();n.setNode(r,{});for(var a,i;Ln(n,e)<t;)a=Pn(n,e),i=n.hasNode(a.v)?P(e,a):-P(e,a),Cn(n,e,i);return n}function Ln(e,n){function r(t){f(n.nodeEdges(t),function(a){var i=a.v,o=t===i?a.w:i;!e.hasNode(o)&&!P(n,a)&&(e.setNode(o,{}),e.setEdge(t,o,{}),r(o))})}return f(e.nodes(),r),e.nodeCount()}function Pn(e,n){return U(n.edges(),function(r){if(e.hasNode(r.v)!==e.hasNode(r.w))return P(n,r)})}function Cn(e,n,r){f(e.nodes(),function(t){n.node(t).rank+=r})}function Rn(){}Rn.prototype=new Error;function ke(e,n,r){he(n)||(n=[n]);var t=(e.isDirected()?e.successors:e.neighbors).bind(e),a=[],i={};return f(n,function(o){if(!e.hasNode(o))throw new Error("Graph does not have node: "+o);xe(e,o,r==="post",i,t,a)}),a}function xe(e,n,r,t,a,i){Object.prototype.hasOwnProperty.call(t,n)||(t[n]=!0,r||i.push(n),f(a(n),function(o){xe(e,o,r,t,a,i)}),r&&i.push(n))}function _n(e,n){return ke(e,n,"post")}function Tn(e,n){return ke(e,n,"pre")}k.initLowLimValues=K;k.initCutValues=Z;k.calcCutValue=Ee;k.leaveEdge=Ne;k.enterEdge=Le;k.exchangeEdges=Pe;function k(e){e=hn(e),J(e);var n=ye(e);K(n),Z(n,e);for(var r,t;r=Ne(n);)t=Le(n,e,r),Pe(n,e,r,t)}function Z(e,n){var r=_n(e,e.nodes());r=r.slice(0,r.length-1),f(r,function(t){Mn(e,n,t)})}function Mn(e,n,r){var t=e.node(r),a=t.parent;e.edge(r,a).cutvalue=Ee(e,n,r)}function Ee(e,n,r){var t=e.node(r),a=t.parent,i=!0,o=n.edge(r,a),u=0;return o||(i=!1,o=n.edge(a,r)),u=o.weight,f(n.nodeEdges(r),function(d){var s=d.v===r,c=s?d.w:d.v;if(c!==a){var l=s===i,h=n.edge(d).weight;if(u+=l?h:-h,jn(e,r,c)){var v=e.edge(r,c).cutvalue;u+=l?-v:v}}}),u}function K(e,n){arguments.length<2&&(n=e.nodes()[0]),Oe(e,{},1,n)}function Oe(e,n,r,t,a){var i=r,o=e.node(t);return n[t]=!0,f(e.neighbors(t),function(u){Object.prototype.hasOwnProperty.call(n,u)||(r=Oe(e,n,r,u,t))}),o.low=i,o.lim=r++,a?o.parent=a:delete o.parent,r}function Ne(e){return z(e.edges(),function(n){return e.edge(n).cutvalue<0})}function Le(e,n,r){var t=r.v,a=r.w;n.hasEdge(t,a)||(t=r.w,a=r.v);var i=e.node(t),o=e.node(a),u=i,d=!1;i.lim>o.lim&&(u=o,d=!0);var s=R(n.edges(),function(c){return d===oe(e,e.node(c.v),u)&&d!==oe(e,e.node(c.w),u)});return U(s,function(c){return P(n,c)})}function Pe(e,n,r,t){var a=r.v,i=r.w;e.removeEdge(a,i),e.setEdge(t.v,t.w,{}),K(e),Z(e,n),In(e,n)}function In(e,n){var r=z(e.nodes(),function(a){return!n.node(a).parent}),t=Tn(e,r);t=t.slice(1),f(t,function(a){var i=e.node(a).parent,o=n.edge(a,i),u=!1;o||(o=n.edge(i,a),u=!0),n.node(a).rank=n.node(i).rank+(u?o.minlen:-o.minlen)})}function jn(e,n,r){return e.hasEdge(n,r)}function oe(e,n,r){return r.low<=n.lim&&n.lim<=r.lim}function Sn(e){switch(e.graph().ranker){case"network-simplex":ue(e);break;case"tight-tree":Vn(e);break;case"longest-path":Fn(e);break;default:ue(e)}}var Fn=J;function Vn(e){J(e),ye(e)}function ue(e){k(e)}function Bn(e){var n=O(e,"root",{},"_root"),r=An(e),t=y(x(r))-1,a=2*t+1;e.graph().nestingRoot=n,f(e.edges(),function(o){e.edge(o).minlen*=a});var i=Yn(e)+1;f(e.children(),function(o){Ce(e,n,a,i,t,r,o)}),e.graph().nodeRankFactor=a}function Ce(e,n,r,t,a,i,o){var u=e.children(o);if(!u.length){o!==n&&e.setEdge(n,o,{weight:0,minlen:r});return}var d=te(e,"_bt"),s=te(e,"_bb"),c=e.node(o);e.setParent(d,o),c.borderTop=d,e.setParent(s,o),c.borderBottom=s,f(u,function(l){Ce(e,n,r,t,a,i,l);var h=e.node(l),v=h.borderTop?h.borderTop:l,p=h.borderBottom?h.borderBottom:l,b=h.borderTop?t:2*t,N=v!==p?1:a-i[o]+1;e.setEdge(d,v,{weight:b,minlen:N,nestingEdge:!0}),e.setEdge(p,s,{weight:b,minlen:N,nestingEdge:!0})}),e.parent(o)||e.setEdge(n,d,{weight:0,minlen:a+i[o]})}function An(e){var n={};function r(t,a){var i=e.children(t);i&&i.length&&f(i,function(o){r(o,a+1)}),n[t]=a}return f(e.children(),function(t){r(t,1)}),n}function Yn(e){return I(e.edges(),function(n,r){return n+e.edge(r).weight},0)}function Gn(e){var n=e.graph();e.removeNode(n.nestingRoot),delete n.nestingRoot,f(e.edges(),function(r){var t=e.edge(r);t.nestingEdge&&e.removeEdge(r)})}function Dn(e,n,r){var t={},a;f(r,function(i){for(var o=e.parent(i),u,d;o;){if(u=e.parent(o),u?(d=t[u],t[u]=o):(d=a,a=o),d&&d!==o){n.setEdge(d,o);return}o=u}})}function qn(e,n,r){var t=$n(e),a=new g({compound:!0}).setGraph({root:t}).setDefaultNodeLabel(function(i){return e.node(i)});return f(e.nodes(),function(i){var o=e.node(i),u=e.parent(i);(o.rank===n||o.minRank<=n&&n<=o.maxRank)&&(a.setNode(i),a.setParent(i,u||t),f(e[r](i),function(d){var s=d.v===i?d.w:d.v,c=a.edge(s,i),l=m(c)?0:c.weight;a.setEdge(s,i,{weight:e.edge(d).weight+l})}),Object.prototype.hasOwnProperty.call(o,"minRank")&&a.setNode(i,{borderLeft:o.borderLeft[n],borderRight:o.borderRight[n]}))}),a}function $n(e){for(var n;e.hasNode(n=H("_root")););return n}function Wn(e,n){for(var r=0,t=1;t<n.length;++t)r+=Xn(e,n[t-1],n[t]);return r}function Xn(e,n,r){for(var t=tn(r,w(r,function(s,c){return c})),a=C(w(n,function(s){return _(w(e.outEdges(s),function(c){return{pos:t[c.w],weight:e.edge(c).weight}}),"pos")})),i=1;i<r.length;)i<<=1;var o=2*i-1;i-=1;var u=w(new Array(o),function(){return 0}),d=0;return f(a.forEach(function(s){var c=s.pos+i;u[c]+=s.weight;for(var l=0;c>0;)c%2&&(l+=u[c+1]),c=c-1>>1,u[c]+=s.weight;d+=s.weight*l})),d}function zn(e){var n={},r=R(e.nodes(),function(u){return!e.children(u).length}),t=y(w(r,function(u){return e.node(u).rank})),a=w(E(t+1),function(){return[]});function i(u){if(!we(n,u)){n[u]=!0;var d=e.node(u);a[d.rank].push(u),f(e.successors(u),i)}}var o=_(r,function(u){return e.node(u).rank});return f(o,i),a}function Un(e,n){return w(n,function(r){var t=e.inEdges(r);if(t.length){var a=I(t,function(i,o){var u=e.edge(o),d=e.node(o.v);return{sum:i.sum+u.weight*d.order,weight:i.weight+u.weight}},{sum:0,weight:0});return{v:r,barycenter:a.sum/a.weight,weight:a.weight}}else return{v:r}})}function Hn(e,n){var r={};f(e,function(a,i){var o=r[a.v]={indegree:0,in:[],out:[],vs:[a.v],i};m(a.barycenter)||(o.barycenter=a.barycenter,o.weight=a.weight)}),f(n.edges(),function(a){var i=r[a.v],o=r[a.w];!m(i)&&!m(o)&&(o.indegree++,i.out.push(r[a.w]))});var t=R(r,function(a){return!a.indegree});return Jn(t)}function Jn(e){var n=[];function r(i){return function(o){o.merged||(m(o.barycenter)||m(i.barycenter)||o.barycenter>=i.barycenter)&&Zn(i,o)}}function t(i){return function(o){o.in.push(i),--o.indegree===0&&e.push(o)}}for(;e.length;){var a=e.pop();n.push(a),f(a.in.reverse(),r(a)),f(a.out,t(a))}return w(R(n,function(i){return!i.merged}),function(i){return T(i,["vs","i","barycenter","weight"])})}function Zn(e,n){var r=0,t=0;e.weight&&(r+=e.barycenter*e.weight,t+=e.weight),n.weight&&(r+=n.barycenter*n.weight,t+=n.weight),e.vs=n.vs.concat(e.vs),e.barycenter=r/t,e.weight=t,e.i=Math.min(n.i,e.i),n.merged=!0}function Kn(e,n){var r=wn(e,function(c){return Object.prototype.hasOwnProperty.call(c,"barycenter")}),t=r.lhs,a=_(r.rhs,function(c){return-c.i}),i=[],o=0,u=0,d=0;t.sort(Qn(!!n)),d=de(i,a,d),f(t,function(c){d+=c.vs.length,i.push(c.vs),o+=c.barycenter*c.weight,u+=c.weight,d=de(i,a,d)});var s={vs:C(i)};return u&&(s.barycenter=o/u,s.weight=u),s}function de(e,n,r){for(var t;n.length&&(t=M(n)).i<=r;)n.pop(),e.push(t.vs),r++;return r}function Qn(e){return function(n,r){return n.barycenter<r.barycenter?-1:n.barycenter>r.barycenter?1:e?r.i-n.i:n.i-r.i}}function Re(e,n,r,t){var a=e.children(n),i=e.node(n),o=i?i.borderLeft:void 0,u=i?i.borderRight:void 0,d={};o&&(a=R(a,function(p){return p!==o&&p!==u}));var s=Un(e,a);f(s,function(p){if(e.children(p.v).length){var b=Re(e,p.v,r,t);d[p.v]=b,Object.prototype.hasOwnProperty.call(b,"barycenter")&&nr(p,b)}});var c=Hn(s,r);er(c,d);var l=Kn(c,t);if(o&&(l.vs=C([o,l.vs,u]),e.predecessors(o).length)){var h=e.node(e.predecessors(o)[0]),v=e.node(e.predecessors(u)[0]);Object.prototype.hasOwnProperty.call(l,"barycenter")||(l.barycenter=0,l.weight=0),l.barycenter=(l.barycenter*l.weight+h.order+v.order)/(l.weight+2),l.weight+=2}return l}function er(e,n){f(e,function(r){r.vs=C(r.vs.map(function(t){return n[t]?n[t].vs:t}))})}function nr(e,n){m(e.barycenter)?(e.barycenter=n.barycenter,e.weight=n.weight):(e.barycenter=(e.barycenter*e.weight+n.barycenter*n.weight)/(e.weight+n.weight),e.weight+=n.weight)}function rr(e){var n=me(e),r=se(e,E(1,n+1),"inEdges"),t=se(e,E(n-1,-1,-1),"outEdges"),a=zn(e);fe(e,a);for(var i=Number.POSITIVE_INFINITY,o,u=0,d=0;d<4;++u,++d){tr(u%2?r:t,u%4>=2),a=S(e);var s=Wn(e,a);s<i&&(d=0,o=Ge(a),i=s)}fe(e,o)}function se(e,n,r){return w(n,function(t){return qn(e,t,r)})}function tr(e,n){var r=new g;f(e,function(t){var a=t.graph().root,i=Re(t,a,r,n);f(i.vs,function(o,u){t.node(o).order=u}),Dn(t,r,i.vs)})}function fe(e,n){f(n,function(r){f(r,function(t,a){e.node(t).order=a})})}function ar(e){var n=or(e);f(e.graph().dummyChains,function(r){for(var t=e.node(r),a=t.edgeObj,i=ir(e,n,a.v,a.w),o=i.path,u=i.lca,d=0,s=o[d],c=!0;r!==a.w;){if(t=e.node(r),c){for(;(s=o[d])!==u&&e.node(s).maxRank<t.rank;)d++;s===u&&(c=!1)}if(!c){for(;d<o.length-1&&e.node(s=o[d+1]).minRank<=t.rank;)d++;s=o[d]}e.setParent(r,s),r=e.successors(r)[0]}})}function ir(e,n,r,t){var a=[],i=[],o=Math.min(n[r].low,n[t].low),u=Math.max(n[r].lim,n[t].lim),d,s;d=r;do d=e.parent(d),a.push(d);while(d&&(n[d].low>o||u>n[d].lim));for(s=d,d=t;(d=e.parent(d))!==s;)i.push(d);return{path:a.concat(i.reverse()),lca:s}}function or(e){var n={},r=0;function t(a){var i=r;f(e.children(a),t),n[a]={low:i,lim:r++}}return f(e.children(),t),n}function ur(e,n){var r={};function t(a,i){var o=0,u=0,d=a.length,s=M(i);return f(i,function(c,l){var h=sr(e,c),v=h?e.node(h).order:d;(h||c===s)&&(f(i.slice(u,l+1),function(p){f(e.predecessors(p),function(b){var N=e.node(b),Q=N.order;(Q<o||v<Q)&&!(N.dummy&&e.node(p).dummy)&&_e(r,b,p)})}),u=l+1,o=v)}),i}return I(n,t),r}function dr(e,n){var r={};function t(i,o,u,d,s){var c;f(E(o,u),function(l){c=i[l],e.node(c).dummy&&f(e.predecessors(c),function(h){var v=e.node(h);v.dummy&&(v.order<d||v.order>s)&&_e(r,h,c)})})}function a(i,o){var u=-1,d,s=0;return f(o,function(c,l){if(e.node(c).dummy==="border"){var h=e.predecessors(c);h.length&&(d=e.node(h[0]).order,t(o,s,l,u,d),s=l,u=d)}t(o,s,o.length,d,i.length)}),o}return I(n,a),r}function sr(e,n){if(e.node(n).dummy)return z(e.predecessors(n),function(r){return e.node(r).dummy})}function _e(e,n,r){if(n>r){var t=n;n=r,r=t}var a=e[n];a||(e[n]=a={}),a[r]=!0}function fr(e,n,r){if(n>r){var t=n;n=r,r=t}return!!e[n]&&Object.prototype.hasOwnProperty.call(e[n],r)}function cr(e,n,r,t){var a={},i={},o={};return f(n,function(u){f(u,function(d,s){a[d]=d,i[d]=d,o[d]=s})}),f(n,function(u){var d=-1;f(u,function(s){var c=t(s);if(c.length){c=_(c,function(b){return o[b]});for(var l=(c.length-1)/2,h=Math.floor(l),v=Math.ceil(l);h<=v;++h){var p=c[h];i[s]===s&&d<o[p]&&!fr(r,s,p)&&(i[p]=s,i[s]=a[s]=a[p],d=o[p])}}})}),{root:a,align:i}}function lr(e,n,r,t,a){var i={},o=hr(e,n,r,a),u=a?"borderLeft":"borderRight";function d(l,h){for(var v=o.nodes(),p=v.pop(),b={};p;)b[p]?l(p):(b[p]=!0,v.push(p),v=v.concat(h(p))),p=v.pop()}function s(l){i[l]=o.inEdges(l).reduce(function(h,v){return Math.max(h,i[v.v]+o.edge(v))},0)}function c(l){var h=o.outEdges(l).reduce(function(p,b){return Math.min(p,i[b.w]-o.edge(b))},Number.POSITIVE_INFINITY),v=e.node(l);h!==Number.POSITIVE_INFINITY&&v.borderType!==u&&(i[l]=Math.max(i[l],h))}return d(s,o.predecessors.bind(o)),d(c,o.successors.bind(o)),f(t,function(l){i[l]=i[r[l]]}),i}function hr(e,n,r,t){var a=new g,i=e.graph(),o=mr(i.nodesep,i.edgesep,t);return f(n,function(u){var d;f(u,function(s){var c=r[s];if(a.setNode(c),d){var l=r[d],h=a.edge(l,c);a.setEdge(l,c,Math.max(o(e,s,d),h||0))}d=s})}),a}function vr(e,n){return U(x(n),function(r){var t=Number.NEGATIVE_INFINITY,a=Number.POSITIVE_INFINITY;return $e(r,function(i,o){var u=gr(e,o)/2;t=Math.max(i+u,t),a=Math.min(i-u,a)}),t-a})}function pr(e,n){var r=x(n),t=L(r),a=y(r);f(["u","d"],function(i){f(["l","r"],function(o){var u=i+o,d=e[u],s;if(d!==n){var c=x(d);s=o==="l"?t-L(c):a-y(c),s&&(e[u]=j(d,function(l){return l+s}))}})})}function wr(e,n){return j(e.ul,function(r,t){if(n)return e[n.toLowerCase()][t];var a=_(w(e,t));return(a[1]+a[2])/2})}function br(e){var n=S(e),r=$(ur(e,n),dr(e,n)),t={},a;f(["u","d"],function(o){a=o==="u"?n:x(n).reverse(),f(["l","r"],function(u){u==="r"&&(a=w(a,function(l){return x(l).reverse()}));var d=(o==="u"?e.predecessors:e.successors).bind(e),s=cr(e,a,r,d),c=lr(e,a,s.root,s.align,u==="r");u==="r"&&(c=j(c,function(l){return-l})),t[o+u]=c})});var i=vr(e,t);return pr(t,i),wr(t,e.graph().align)}function mr(e,n,r){return function(t,a,i){var o=t.node(a),u=t.node(i),d=0,s;if(d+=o.width/2,Object.prototype.hasOwnProperty.call(o,"labelpos"))switch(o.labelpos.toLowerCase()){case"l":s=-o.width/2;break;case"r":s=o.width/2;break}if(s&&(d+=r?s:-s),s=0,d+=(o.dummy?n:e)/2,d+=(u.dummy?n:e)/2,d+=u.width/2,Object.prototype.hasOwnProperty.call(u,"labelpos"))switch(u.labelpos.toLowerCase()){case"l":s=u.width/2;break;case"r":s=-u.width/2;break}return s&&(d+=r?s:-s),s=0,d}}function gr(e,n){return e.node(n).width}function yr(e){e=be(e),kr(e),We(br(e),function(n,r){e.node(r).x=n})}function kr(e){var n=S(e),r=e.graph().ranksep,t=0;f(n,function(a){var i=y(w(a,function(o){return e.node(o).height}));f(a,function(o){e.node(o).y=t+i/2}),t+=i+r})}function Hr(e,n){var r=bn;r("layout",()=>{var t=r("  buildLayoutGraph",()=>Mr(e));r("  runLayout",()=>xr(t,r)),r("  updateInputGraph",()=>Er(e,t))})}function xr(e,n){n("    makeSpaceForEdgeLabels",()=>Ir(e)),n("    removeSelfEdges",()=>Dr(e)),n("    acyclic",()=>fn(e)),n("    nestingGraph.run",()=>Bn(e)),n("    rank",()=>Sn(be(e))),n("    injectEdgeLabelProxies",()=>jr(e)),n("    removeEmptyRanks",()=>pn(e)),n("    nestingGraph.cleanup",()=>Gn(e)),n("    normalizeRanks",()=>vn(e)),n("    assignRankMinMax",()=>Sr(e)),n("    removeEdgeLabelProxies",()=>Fr(e)),n("    normalize.run",()=>En(e)),n("    parentDummyChains",()=>ar(e)),n("    addBorderSegments",()=>mn(e)),n("    order",()=>rr(e)),n("    insertSelfEdges",()=>qr(e)),n("    adjustCoordinateSystem",()=>gn(e)),n("    position",()=>yr(e)),n("    positionSelfEdges",()=>$r(e)),n("    removeBorderNodes",()=>Gr(e)),n("    normalize.undo",()=>Nn(e)),n("    fixupEdgeLabelCoords",()=>Ar(e)),n("    undoCoordinateSystem",()=>yn(e)),n("    translateGraph",()=>Vr(e)),n("    assignNodeIntersects",()=>Br(e)),n("    reversePoints",()=>Yr(e)),n("    acyclic.undo",()=>ln(e))}function Er(e,n){f(e.nodes(),function(r){var t=e.node(r),a=n.node(r);t&&(t.x=a.x,t.y=a.y,n.children(r).length&&(t.width=a.width,t.height=a.height))}),f(e.edges(),function(r){var t=e.edge(r),a=n.edge(r);t.points=a.points,Object.prototype.hasOwnProperty.call(a,"x")&&(t.x=a.x,t.y=a.y)}),e.graph().width=n.graph().width,e.graph().height=n.graph().height}var Or=["nodesep","edgesep","ranksep","marginx","marginy"],Nr={ranksep:50,edgesep:20,nodesep:50,rankdir:"tb"},Lr=["acyclicer","ranker","rankdir","align"],Pr=["width","height"],Cr={width:0,height:0},Rr=["minlen","weight","width","height","labeloffset"],_r={minlen:1,weight:1,width:0,height:0,labeloffset:10,labelpos:"r"},Tr=["labelpos"];function Mr(e){var n=new g({multigraph:!0,compound:!0}),r=D(e.graph());return n.setGraph($({},Nr,G(r,Or),T(r,Lr))),f(e.nodes(),function(t){var a=D(e.node(t));n.setNode(t,qe(G(a,Pr),Cr)),n.setParent(t,e.parent(t))}),f(e.edges(),function(t){var a=D(e.edge(t));n.setEdge(t,$({},_r,G(a,Rr),T(a,Tr)))}),n}function Ir(e){var n=e.graph();n.ranksep/=2,f(e.edges(),function(r){var t=e.edge(r);t.minlen*=2,t.labelpos.toLowerCase()!=="c"&&(n.rankdir==="TB"||n.rankdir==="BT"?t.width+=t.labeloffset:t.height+=t.labeloffset)})}function jr(e){f(e.edges(),function(n){var r=e.edge(n);if(r.width&&r.height){var t=e.node(n.v),a=e.node(n.w),i={rank:(a.rank-t.rank)/2+t.rank,e:n};O(e,"edge-proxy",i,"_ep")}})}function Sr(e){var n=0;f(e.nodes(),function(r){var t=e.node(r);t.borderTop&&(t.minRank=e.node(t.borderTop).rank,t.maxRank=e.node(t.borderBottom).rank,n=y(n,t.maxRank))}),e.graph().maxRank=n}function Fr(e){f(e.nodes(),function(n){var r=e.node(n);r.dummy==="edge-proxy"&&(e.edge(r.e).labelRank=r.rank,e.removeNode(n))})}function Vr(e){var n=Number.POSITIVE_INFINITY,r=0,t=Number.POSITIVE_INFINITY,a=0,i=e.graph(),o=i.marginx||0,u=i.marginy||0;function d(s){var c=s.x,l=s.y,h=s.width,v=s.height;n=Math.min(n,c-h/2),r=Math.max(r,c+h/2),t=Math.min(t,l-v/2),a=Math.max(a,l+v/2)}f(e.nodes(),function(s){d(e.node(s))}),f(e.edges(),function(s){var c=e.edge(s);Object.prototype.hasOwnProperty.call(c,"x")&&d(c)}),n-=o,t-=u,f(e.nodes(),function(s){var c=e.node(s);c.x-=n,c.y-=t}),f(e.edges(),function(s){var c=e.edge(s);f(c.points,function(l){l.x-=n,l.y-=t}),Object.prototype.hasOwnProperty.call(c,"x")&&(c.x-=n),Object.prototype.hasOwnProperty.call(c,"y")&&(c.y-=t)}),i.width=r-n+o,i.height=a-t+u}function Br(e){f(e.edges(),function(n){var r=e.edge(n),t=e.node(n.v),a=e.node(n.w),i,o;r.points?(i=r.points[0],o=r.points[r.points.length-1]):(r.points=[],i=a,o=t),r.points.unshift(re(t,i)),r.points.push(re(a,o))})}function Ar(e){f(e.edges(),function(n){var r=e.edge(n);if(Object.prototype.hasOwnProperty.call(r,"x"))switch((r.labelpos==="l"||r.labelpos==="r")&&(r.width-=r.labeloffset),r.labelpos){case"l":r.x-=r.width/2+r.labeloffset;break;case"r":r.x+=r.width/2+r.labeloffset;break}})}function Yr(e){f(e.edges(),function(n){var r=e.edge(n);r.reversed&&r.points.reverse()})}function Gr(e){f(e.nodes(),function(n){if(e.children(n).length){var r=e.node(n),t=e.node(r.borderTop),a=e.node(r.borderBottom),i=e.node(M(r.borderLeft)),o=e.node(M(r.borderRight));r.width=Math.abs(o.x-i.x),r.height=Math.abs(a.y-t.y),r.x=i.x+r.width/2,r.y=t.y+r.height/2}}),f(e.nodes(),function(n){e.node(n).dummy==="border"&&e.removeNode(n)})}function Dr(e){f(e.edges(),function(n){if(n.v===n.w){var r=e.node(n.v);r.selfEdges||(r.selfEdges=[]),r.selfEdges.push({e:n,label:e.edge(n)}),e.removeEdge(n)}})}function qr(e){var n=S(e);f(n,function(r){var t=0;f(r,function(a,i){var o=e.node(a);o.order=i+t,f(o.selfEdges,function(u){O(e,"selfedge",{width:u.label.width,height:u.label.height,rank:o.rank,order:i+ ++t,e:u.e,label:u.label},"_se")}),delete o.selfEdges})})}function $r(e){f(e.nodes(),function(n){var r=e.node(n);if(r.dummy==="selfedge"){var t=e.node(r.e.v),a=t.x+t.width/2,i=t.y,o=r.x-a,u=t.height/2;e.setEdge(r.e,r.label),e.removeNode(n),r.label.points=[{x:a+2*o/3,y:i-u},{x:a+5*o/6,y:i-u},{x:a+o,y:i},{x:a+5*o/6,y:i+u},{x:a+2*o/3,y:i+u}],r.label.x=r.x,r.label.y=r.y}})}function G(e,n){return j(T(e,n),Number)}function D(e){var n={};return f(e,function(r,t){n[t.toLowerCase()]=r}),n}export{Hr as l};
