package so.nian.backup.bizz.dao;

import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

public abstract class BaseDao extends SqlSessionDaoSupport {
	
	@Autowired
	@Qualifier("sqlSessionFactory")
	public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
		super.setSqlSessionFactory(sqlSessionFactory);
	}

	public <T> List<T> query(String sqlId, Object paramObject, int offset, int size) {
		List<T> list = null;
		if ((offset >= 0) && (size > 0)) {
			RowBounds rowBounds = new RowBounds(offset, size);
			list = getSqlSession().selectList(sqlId, paramObject, rowBounds);
		} else {
			if (paramObject != null)
				list = getSqlSession().selectList(sqlId, paramObject);
			else
				list = getSqlSession().selectList(sqlId);
		}
		return list;
	}

	public <T> List<T> query(String sqlId, Object paramObject) {
		return this.query(sqlId, paramObject, 0, 0);
	}

	public <T> List<T> query(String sqlId, int offset, int size) {
		return this.query(sqlId, null, offset, size);
	}

	public <T> List<T> query(String sqlId) {
		return this.query(sqlId, null, 0, 0);
	}
	
	public void update(String sqlId, Object parameter) {
		getSqlSession().update(sqlId, parameter);
	}
	
}
